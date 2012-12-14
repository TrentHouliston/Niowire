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
import io.niowire.serversource.Event;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.serversource.NioServerSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
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
	//The selector picking which socket to do next
	private final Selector channels;
	//Byte buffer for reading data into
	private final ByteBuffer buffer;
	//The last time we checked for connections which have timed out
	private long lastTimeout = 0;
	//The server source
	private final NioServerSource source;
	//The servers
	private final HashMap<String, SelectionKey> servers = new HashMap<String, SelectionKey>(1);

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
			//Store our source
			this.source = source;

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
		while (true)
		{
			try
			{
				//Only block for one second so that we can check the timeouts
				//and if any servers are to be added or removed
				channels.select(1000);

				//Iterate through all the keys
				Iterator<SelectionKey> keys = channels.selectedKeys().iterator();
				while (keys.hasNext())
				{
					SelectionKey key = keys.next();

					if (!key.isValid())
					{
						//Log that the connection has closed
						LOG.info("Server {} has disconnected", key.attachment());
						if (key.attachment() instanceof NioConnection)
						{
							//Close and cleanup
							NioConnection con = (NioConnection) key.attachment();
							con.close();
						}
					}
					//If it is one of the servers getting a new connection
					else if (key.isAcceptable())
					{
						//Cast to our server
						ServerSocketChannel server = (ServerSocketChannel) key.channel();

						//Get the server definition which is attached
						NioServerDefinition serverConfig = (NioServerDefinition) key.attachment();

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

							//Log that we have a new connection
							LOG.info("Server {} has connected", client.socket().getInetAddress().getHostAddress());
						}
						catch (RuntimeException ex)
						{
							//If we have an exception then we need to kick the client
							LOG.error("Server {} was rejected as an exception occured during its creation", client.socket().getInetAddress().getHostAddress());

							//Close the client and the key
							client.close();
							clientKey.cancel();
						}
						catch (Exception ex)
						{
							//If we have an exception then we need to kick the client
							LOG.error("Server {} was rejected as an exception occured during its creation", client.socket().getInetAddress().getHostAddress());

							//Close the client and the key
							client.close();
							clientKey.cancel();
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
						if (read == -1)
						{
							//TODO this means that the connection is closed (we reached the end of the stream)
							//(shouldnt !isvalid have been thrown? it might i'm not sure yet
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
								//TODO handle this case
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

					//We are done with this key
					keys.remove();
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

				//Update any servers which have changed in the server source
				for (Entry<NioServerDefinition, Event> server : source.getChanges().entrySet())
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

			}
			catch (CancelledKeyException ex)
			{
				//We really don't care if the keys is cancelled. We probably did it.
			}
			catch (IOException ex)
			{
				if (ex.getMessage().equals("Connection reset by peer"))
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
	 * This method adds a new server into the Socket Server.
	 *
	 * @param server the server definition to add
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	protected void addServer(NioServerDefinition server) throws IOException
	{
		ServerSocketChannel serv = ServerSocketChannel.open();
		serv.configureBlocking(false);

		if (server.getPort() != null)
		{
			serv.bind(new InetSocketAddress(server.getPort()));
		}
		else
		{
			serv.bind(null);
			server.setPort(serv.socket().getLocalPort());
		}

		SelectionKey key = serv.register(channels, SelectionKey.OP_ACCEPT);
		key.attach(server);

		servers.put(server.getId(), key);
	}

	/**
	 * This method removes a server from the Socket Server.
	 *
	 * @param server the server definition to remove
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	protected void removeServer(NioServerDefinition server) throws IOException
	{
		servers.get(server.getId()).channel().close();
		servers.remove(server.getId());
	}

	/**
	 * This method modifies an existing server definition in the system. If a
	 * NioPropertyUnchangableException occurs then we need to remove and
	 * recreate this server.
	 *
	 * @param server the server definition to add
	 *
	 * @throws IOException if there was an IOException while setting up the
	 *                        channel
	 */
	protected void updateServer(NioServerDefinition server) throws IOException
	{
		//Get the server we are updating
		SelectionKey key = servers.get(server.getId());
		NioServerDefinition current = (NioServerDefinition) key.attachment();
		try
		{
			//Try to update the server
			current.update(server);
		}
		//If we can't update it then remove and readd it
		catch (NioPropertyUnchangableException ex)
		{
			//Remove the server
			removeServer(server);

			//Re Add the server
			addServer(server);
		}
	}
}
