/**
 * This file is part of Niowire.
 *
 * Niowire is free software: you can redistribute it and/or modify it under the
 * terms of the Lesser GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Niowire is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more
 * details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Niowire. If not, see <http://www.gnu.org/licenses/>.
 */
package io.niowire.server;

import io.niowire.NiowireException;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.inspection.TimeoutInspector;
import io.niowire.serializer.LineSerializer;
import io.niowire.serializer.NioSerializer;
import io.niowire.serversource.Event;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.serversource.NioServerSource;
import io.niowire.service.NioService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is the main Socket server and thread object which is run in the
 * Niowire framework. It is the object which listens for connections and data
 * and drives all of the Niowire framework methods. It does this using a
 * Selector which selects channels based on the properties of the connection
 * objects that manage them. This makes it a very high performance server and
 * very scalable.
 *
 * @author Trent Houliston
 */
public class NioSocketServer implements Runnable
{

	//The logger for the server
	private static final Logger LOG = LoggerFactory.getLogger(NioSocketServer.class);
	//The default Serializer
	static final NioObjectFactory<LineSerializer> DEFAULT_SERIALIZER = new NioObjectFactory<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", Charset.defaultCharset().name()));
	//The default inspector
	static final NioObjectFactory<TimeoutInspector> DEFAULT_INSPECTOR = new NioObjectFactory<TimeoutInspector>(TimeoutInspector.class, Collections.singletonMap("timeout", -1));
	//Our thread group
	public final ThreadGroup NIOTHREAD_GROUP = new ThreadGroup("Niowire");
	//The selector picking which socket to do next
	private final Selector channels;
	//Byte buffer for reading data into
	private final ByteBuffer buffer;
	//The last time we checked for connections which have timed out
	private long lastTimeout = 0;
	//The server source execution object
	private final SourceRunner sourceRunner;
	//The server source execution thread
	private final Thread sourceRunnerThread;
	//The servers
	private final HashMap<String, SelectionKey> servers = new HashMap<String, SelectionKey>(1);
	//If we should shutdown
	private boolean shutdownNow = false;
	//Our lock so that we can add/remove servers as needed
	private final Object lock = new Object();

	/**
	 * This creates a new NioSocketServer instance which is managed manually
	 * (servers are added/removed directly rather then via the server source)
	 *
	 * @throws NiowireException if there was an exception while setting up the
	 *                             server.
	 */
	public NioSocketServer() throws NiowireException
	{
		this(null);
	}

	/**
	 * This creates a new NioSocketServer instance which uses the passed
	 * NioServerSource as it's source of server definitions.
	 *
	 * @param source the source of server definitions for this server
	 *
	 * @throws NiowireException if there was an exception while setting up the
	 *                             server.
	 */
	public NioSocketServer(NioServerSource source) throws NiowireException
	{
		try
		{
			//Allocate 8k for our buffer to read from sockets
			buffer = ByteBuffer.allocateDirect(8192);

			//Get a selector for our channels
			channels = Selector.open();
		}
		catch (RuntimeException ex)
		{
			throw new NiowireException("Was unable to setup the server due to an error", ex);
		}
		catch (Exception ex)
		{
			throw new NiowireException("Was unable to setup the server due to an error", ex);
		}

		//If we got passed null as the source that means that they are going
		//to manage the servers manually
		if (source != null)
		{
			//Create a new SourceRunner with the passed source
			this.sourceRunner = new SourceRunner(source);
			this.sourceRunnerThread = new Thread(NIOTHREAD_GROUP, this.sourceRunner, "SourceRunner");
			this.sourceRunnerThread.setDaemon(true);
		}
		else
		{
			this.sourceRunner = null;
			this.sourceRunnerThread = null;
		}
	}

	/**
	 * This is the main loop method which manages all of the operations of the
	 * server. It handles all of the channels via the selector, and will also
	 * handle any timeouts which have occurred and checking of there are any new
	 * or modified server definitions which it must reload.
	 */
	@Override
	public void run()
	{
		//Start up our source runner thread
		if (this.sourceRunnerThread != null)
		{
			this.sourceRunnerThread.start();
		}

		while (true)
		{
			try
			{
				//Only block for one second so that we can check the timeouts
				//and if any servers are to be added or removed
				channels.select(1000);

				synchronized (lock)
				{
					//Check if we need to shutdown
					if (shutdownNow)
					{
						//Loop through all our keys
						for (SelectionKey key : channels.keys())
						{
							try
							{
								//if it's a connection
								if (key.attachment() instanceof NioConnection)
								{
									//Close the connection
									NioConnection con = (NioConnection) key.attachment();
									con.close();
								}
								//Close the socket (graceful disconnection)
								if (key.channel() instanceof SocketChannel)
								{
									((SocketChannel) key.channel()).socket().close();
								}
								//Close and cancel the key/socket (which cancels all keys)
								key.channel().close();
							}
							catch (RuntimeException ex)
							{
								LOG.warn("Exception while shutting down connection {}", key.channel());
							}
						}

						//Interrupt all the threads in the group (signal to shutdown)
						NIOTHREAD_GROUP.interrupt();
						//TODO make all threads which register with this thread respect this

						//Die! (kill the thread)
						throw new ThreadDeath();
					}

					//Iterate through all the keys
					Iterator<SelectionKey> keys = channels.selectedKeys().iterator();

					while (keys.hasNext())
					{
						try
						{
							SelectionKey key = keys.next();

							//If the key is valid then it has closed
							if (!key.isValid())
							{
								if (key.attachment() instanceof NioConnection)
								{
									//Run the connection closed method
									connectionClosed(key);
								}
								else
								{
									LOG.info("Server {} has stopped listening", key.attachment());
								}
							}
							//If it is one of the servers getting a new connection
							else if (key.isAcceptable())
							{
								//Cast to our server
								ServerSocketChannel server = (ServerSocketChannel) key.channel();

								//Get the server definition which is attached
								ActiveServer serverConfig = (ActiveServer) key.attachment();

								//Accept the new connection and set it non blocking
								SocketChannel client = server.accept();
								client.configureBlocking(false);

								//Register it with the channel with read (we always read)
								SelectionKey clientKey = client.register(channels, SelectionKey.OP_READ);

								try
								{
									//Create a new connection object and attach it to the channel
									NioConnection connection = new NioConnection(clientKey, serverConfig);
									clientKey.attach(connection);
									serverConfig.connections.add(connection);

									//Log that we have a new connection
									LOG.info("Client {} has connected", client.socket().getInetAddress().getHostAddress());
								}
								catch (RuntimeException ex)
								{
									//If we have an exception then we need to kick the client
									LOG.error("Client {} was rejected as an exception occured during its creation", client.socket().getInetAddress().getHostAddress());

									//Close the client and the key
									client.close();
								}
								catch (Exception ex)
								{
									//If we have an exception then we need to kick the client
									LOG.error("Client {} was rejected as an exception occured during its creation", client.socket().getInetAddress().getHostAddress());

									//Close the client and the key
									client.close();
								}
							}
							//If the channel is readable (the socket has new data)
							else if (key.isReadable())
							{
								//Cast and get our attachments
								SocketChannel chan = (SocketChannel) key.channel();
								NioConnection connection = (NioConnection) key.attachment();

								//Clear our old data
								buffer.clear();

								int read = chan.read(buffer);
								//If -1 then its the end of the stream (socket closed)
								if (read == -1)
								{
									//Run the closed connection
									connectionClosed(key);
								}
								else
								{
									//Get the buffer ready for writing
									buffer.flip();

									try
									{
										//Write it to our connection
										connection.write(buffer);
									}
									catch (BufferOverflowException ex)
									{
										LOG.error("Buffer overflow exception while attempting to parse the data");
									}
								}
							}
							//If the channel is writeable (we have data to send to the client)
							else if (key.isWritable())
							{
								//Do our casting
								SocketChannel chan = (SocketChannel) key.channel();
								NioConnection connection = (NioConnection) key.attachment();

								//Clear our buffer
								buffer.clear();
								connection.read(buffer);

								//Read into our buffer
								buffer.flip();

								//Write as much data as we can to the client
								chan.write(buffer);

								//If we have data left over then send it back to be rebuffered
								connection.rebuffer(buffer);
							}
						}
						finally
						{
							//We are done with this key (even if there was an exception)
							keys.remove();
						}
					}

					//Check if enough time has passed that we should check the timeouts again
					if (System.currentTimeMillis() - lastTimeout > 1000)
					{
						//Set our last timeout check
						lastTimeout = System.currentTimeMillis();

						//Loop through all the keys
						for (SelectionKey key : channels.keys())
						{
							//Check to see if the connection has timed out (the connection will suicide if it has timed out)
							if (key.attachment() instanceof NioConnection)
							{
								//Tell the connection to check it's timeout
								NioConnection connection = (NioConnection) key.attachment();
								connection.timeout();
							}
						}
					}

				}
			}
			catch (CancelledKeyException ex)
			{
				//We really don't care if the keys is cancelled. We probably did it.
			}
			catch (IOException ex)
			{
				if (ex.getMessage() != null && ex.getMessage().equals("Connection reset by peer"))
				{
					/*
					 * This paticular exception occurs from time to time and
					 * it's massive stack trace fills up the log. So instead we
					 * just ignore it since it is treated as a disconnection.
					 */
				}
				else
				{
					//Otherwise there was an exception of some description that we didn't expect
					LOG.error("There was an exception while executing in the Socket Server", ex);
				}
			}
			//Explicitly catch RuntimeException (we are intentionally catching everything)
			catch (RuntimeException ex)
			{
				//Warn that an exception was thrown (we don't want to crash if we can help it)
				LOG.warn("There was an exception while executing in the Socket Server", ex);
			}
			catch (Exception ex)
			{
				//Warn that an exception was thrown (we don't want to crash if we can help it)
				LOG.warn("There was an exception while executing in the Socket Server", ex);
			}
		}
	}

	/**
	 * This method is run when the server needs to be shutdown, it flags to the
	 * server that it needs to be shut down so that on the next loop of the
	 * system it will close down all the servers and connections it has.
	 */
	public void shutdown()
	{
		this.shutdownNow = true;
		channels.wakeup();
	}

	/**
	 * This method adds a new server into the Socket Server.
	 *
	 * @param serverDef the server definition to add
	 *
	 * @return the port that we bound to
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	public int addServer(NioServerDefinition serverDef) throws IOException
	{
		//Create a server for us to use
		ActiveServer server = new ActiveServer(serverDef);

		//Get a new Socket Channel
		ServerSocketChannel serv = setupServerSocketChannel(server.getPort());
		SelectionKey key;

		synchronized (lock)
		{
			//Wakeup the selector (so we can add the new server straight away)
			channels.wakeup();

			//Register
			key = serv.register(channels, SelectionKey.OP_ACCEPT, server);
		}

		//Put ourselves in our list of active servers
		servers.put(server.getId(), key);

		//Set and return the port we bound to
		int port = serv.socket().getLocalPort();
		server.setPort(port);
		return port;
	}

	/**
	 * This method removes a server from the Socket Server.
	 *
	 * @param server the server definition to remove
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	public void removeServer(NioServerDefinition server) throws IOException
	{
		SelectionKey key = servers.get(server.getId());
		servers.remove(server.getId());

		//Close the channel
		key.channel().close();
		ActiveServer active = (ActiveServer) key.attachment();

		//Create a new list to hold the elements (since we will be mutating it we can't use the original list)
		LinkedList<NioConnection> connections = new LinkedList<NioConnection>(active.connections);

		//Close all the sockets which are connected to this server
		for (NioConnection con : connections)
		{
			con.close();
		}
	}

	/**
	 * This method modifies an existing server definition in the system. If a
	 * NioPortChangedException occurs then we need to remove and recreate this
	 * server.
	 *
	 * @param server the server definition to add
	 *
	 * @return the port number that the server is running on
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	public int updateServer(NioServerDefinition server) throws IOException
	{
		//Get the server we are updating
		SelectionKey key = servers.get(server.getId());
		ActiveServer current = (ActiveServer) key.attachment();
		//Update the current server's details
		if (current.update(server))
		{
			//If the port has been changed then update will return true

			//Get a connection from the new channel
			ServerSocketChannel serv = setupServerSocketChannel(server.getPort());
			SelectionKey newKey;

			synchronized (lock)
			{
				//Wakeup the selector
				channels.wakeup();

				//Register the new channel
				newKey = serv.register(channels, SelectionKey.OP_ACCEPT);
			}

			//Attach to the server
			newKey.attach(current);

			//Overwrite the key in the server
			servers.put(server.getId(), newKey);

			//Close the old channel
			key.channel().close();

			//Set and return the new port
			int port = serv.socket().getLocalPort();
			server.setPort(port);
			return port;
		}
		else
		{
			//Return the existing port
			return ((ServerSocketChannel) key.channel()).socket().getLocalPort();
		}
	}

	/**
	 * Gets the list of active servers which are currently in the system with
	 * their actual implementations
	 *
	 * @return the servers which are active in the system
	 */
	public List<NioServerDefinition> getServers()
	{
		//Create a list to hold the results
		LinkedList<NioServerDefinition> list = new LinkedList<NioServerDefinition>();

		//Get all of our servers
		for (SelectionKey key : servers.values())
		{
			list.add((NioServerDefinition) key.attachment());
		}

		//Return an unmodifyable view of them
		return Collections.unmodifiableList(list);

	}

	/**
	 * This method is run when a client disconnects from a server. It is
	 * necessary as there are two ways that a client can disconnect (either end
	 * of stream or by invalid key)
	 *
	 * @param key the key to clean up
	 *
	 * @throws IOException
	 */
	private void connectionClosed(SelectionKey key) throws IOException
	{
		//Get our connection
		NioConnection con = (NioConnection) key.attachment();

		//Log that the connection has closed
		LOG.info("Client {} has disconnected", con);

		//Clean everything up
		con.close();
		key.channel().close();
	}

	/**
	 * Creates a ServerSocketChannel for the passed port (or a random port if
	 * port is null)
	 *
	 * @param port the port to bind to
	 *
	 * @return the ServerSocketChannel
	 *
	 * @throws IOException
	 */
	private ServerSocketChannel setupServerSocketChannel(Integer port) throws IOException
	{
		//Open a new socket and set it to non blocking
		ServerSocketChannel serv = ServerSocketChannel.open();
		serv.configureBlocking(false);

		//If we have a port that is not null or 0
		if (port != null && port != 0)
		{
			//Bind to that port
			serv.bind(new InetSocketAddress(port));
		}
		else
		{
			//Otherwise bind to a null port (pick a random port)
			serv.bind(null);
		}

		//Return the created server
		return serv;
	}

	/**
	 * This class is responsible for monitoring the ServerSource for any new
	 * sources in it. It should then apply the changes stated by the server
	 * source to the server.
	 */
	private class SourceRunner implements Runnable
	{

		public static final int SLEEP_TIME = 5000;
		/**
		 * The server source
		 */
		private final NioServerSource source;

		/**
		 * Creates a new SourceRunner with the passed source
		 *
		 * @param source the source of the servers
		 */
		private SourceRunner(NioServerSource source)
		{
			this.source = source;
		}

		/**
		 * The run method checks for any new servers and when they are found it
		 * updates the SocketServer. If the source is blocking it will wait
		 * until a source is found, otherwise it will implement a 5 second
		 * polling policy;
		 */
		@Override
		public void run()
		{
			while (!Thread.currentThread().isInterrupted())
			{
				try
				{
					//Update any servers which have changed in the server source
					for (Entry<NioServerDefinition, Event> server : source.getChanges().entrySet())
					{
						try
						{
							//Get our config
							NioServerDefinition config = server.getKey();

							switch (server.getValue())
							{
								//If we are adding a server then add a server
								case SERVER_ADD:
									addServer(config);
									break;
								//If we are removing a server then remove the server
								case SERVER_REMOVE:
									removeServer(config);
									break;
								//If we are updating a server then update the server
								case SERVER_UPDATE:
									updateServer(config);
									break;
							}
						}
						catch (IOException ex)
						{
							LOG.warn("There was an exception while trying to update the server");
						}
					}

					//If it is not a blocking server source then wait 5 seconds
					if (!source.isBlocking())
					{
						Thread.sleep(SLEEP_TIME);
					}
				}
				catch (RuntimeException ex)
				{
				}
				catch (Exception ex)
				{
				}
			}
		}
	}

	/**
	 * This class holds the active server information for the server. This is so
	 * that changing a server definition object does not alter our active state
	 * (as updating it could cause issues if we don't know about it)
	 */
	public static class ActiveServer extends NioServerDefinition
	{

		//Data for this server
		private Integer activePort;
		//Connections made to this server
		private transient List<NioConnection> connections = new LinkedList<NioConnection>();

		/**
		 * Build a new active server from the passed definition
		 *
		 * @param def the server definition we are building ourselves off
		 */
		public ActiveServer(NioServerDefinition def)
		{
			this.id = def.getId();
			this.name = def.getName();
			this.port = def.getPort();
			this.serializerFactory = def.getSerializerFactory() == null ? DEFAULT_SERIALIZER : def.getSerializerFactory();
			this.inspectorFactory = def.getInspectorFactory() == null ? DEFAULT_INSPECTOR : def.getInspectorFactory();
			this.serviceFactories = def.getServiceFactories();
		}

		/**
		 * This method is used to update the server definition object with the
		 * details from the new server object.
		 *
		 * @param def the new server definition to update from
		 *
		 * @return returns true if the port was updated (need to change our
		 *               server)
		 */
		protected boolean update(NioServerDefinition def)
		{
			//Work out if our port needs to be updated, we update in the following cases
			//Our port is null and theirs is not
			//Our port is not null and theirs is not null and different to ours
			boolean portUpdated = (this.port == null && def.getPort() != null)
								  || (this.port != null && def.getPort() != null && this.port.equals(def.getPort()));

			//Update our details
			this.name = def.getName();
			this.serializerFactory = def.getSerializerFactory() == null ? DEFAULT_SERIALIZER : def.getSerializerFactory();
			this.inspectorFactory = def.getInspectorFactory() == null ? DEFAULT_INSPECTOR : def.getInspectorFactory();
			this.serviceFactories = def.getServiceFactories();
			this.port = def.getPort();

			//Loop through our connections and tell them to update themselves
			for (NioConnection con : connections)
			{
				try
				{
					con.updateServerDefinition();
				}
				catch (NioObjectCreationException ex)
				{
					LOG.error("There was an exception while trying to update a connection", ex);
				}
			}

			return portUpdated;
		}

		/**
		 * This is a private setter for the port, as if we are passed a null
		 * port, the port will need to be set later once the connection is made.
		 *
		 * @param port the port to be set
		 */
		private void setPort(int port)
		{
			this.activePort = port;
		}

		/**
		 * Disables setting of the ID on an active server
		 *
		 * @param id the ID
		 */
		@Override
		public void setId(String id)
		{
			throw new UnsupportedOperationException("Cannot set the ID on an active server");
		}

		/**
		 * Gets the port of this active server (the port that is actually in
		 * use)
		 *
		 * @return the port
		 */
		@Override
		public Integer getPort()
		{
			return port != null ? port : activePort;
		}

		/**
		 * Gets the serializer factory for connections to this server
		 *
		 * @param factory the factory
		 */
		@Override
		public void setSerializerFactory(NioObjectFactory<? extends NioSerializer> factory)
		{
			throw new UnsupportedOperationException("Cannot change a factory on an active server, Update instead");
		}

		/**
		 * Disables setting of the InspectorFactory
		 *
		 * @param factory the inspectorFactory
		 */
		@Override
		public void setInspectorFactory(NioObjectFactory<? extends NioInspector> factory)
		{
			throw new UnsupportedOperationException("Cannot change a factory on an active server, Update instead");
		}

		/**
		 * Disables setting of the ServiceFactories
		 *
		 * @param factory the serviceFactories
		 */
		@Override
		public void setServiceFactories(List<? extends NioObjectFactory<? extends NioService>> factory)
		{
			throw new UnsupportedOperationException("Cannot change a factory on an active server, Update instead");
		}

		/**
		 * Removes the passed connection from this server's list of active
		 * connections (should only be accessed by a NioConnection
		 *
		 * @param con the connection to remove
		 */
		void remove(NioConnection con)
		{
			connections.remove(con);
		}
	}
}
