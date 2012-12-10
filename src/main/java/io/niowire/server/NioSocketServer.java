package io.niowire.server;

import io.niowire.NiowireException;
import io.niowire.serversource.Event;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.serversource.NioServerSource;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author trent
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
	private final HashMap<String, SelectionKey> servers = new HashMap<String, SelectionKey>();

	/**
	 * This creates a new NioSocketServer instance which uses the passed
	 * NioServerSource as it's source of server definitions.
	 *
	 * @param source the source of server definitions for this server
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
						LOG.info("Server {} has disconnected", key.attachment());
						//TODO handle closing of the connection, close all service handlers log that it closed, etc
					}
					else if (key.isAcceptable())
					{
						ServerSocketChannel server = (ServerSocketChannel) key.channel();
						NioServerDefinition serverConfig = (NioServerDefinition) key.attachment();

						SocketChannel client = server.accept();
						client.configureBlocking(false);
						SelectionKey clientKey = client.register(channels, SelectionKey.OP_READ);

						try
						{
							NioConnection connection = new NioConnection(clientKey, serverConfig);
							clientKey.attach(connection);
							LOG.info("Server {} has connected", client.socket().getInetAddress().getHostAddress());
						}
						catch (Exception ex)
						{
							LOG.error("Server {} was rejected as an exception occured during its creation", client.socket().getInetAddress().getHostAddress());
							client.close();
							clientKey.cancel();
						}
					}
					else if (key.isReadable())
					{
						SocketChannel chan = (SocketChannel) key.channel();
						NioConnection connection = (NioConnection) key.attachment();

						//Clear our old data
						buffer.clear();

						int read = chan.read(buffer);
						if (read == -1)
						{
							//todo this means that the connection is closed (shouldnt !isvalid have been thrown? it might i'm not sure yet
						}
						else
						{
							//Get the buffer ready for writing
							buffer.flip();
							connection.write(buffer);
						}
					}
					else if (key.isWritable())
					{
						SocketChannel chan = (SocketChannel) key.channel();
						NioConnection connection = (NioConnection) key.attachment();

						buffer.clear();
						connection.read(buffer);
						buffer.flip();
						connection.write(buffer);
						//TODO read as much data as we can into the output channel
						//TODO hopefully the server will update the interest ops of the key
					}

					keys.remove();
				}

				//Check if enough time has passed that we should check the timeouts again
				if (System.currentTimeMillis() - lastTimeout > 1000)
				{
					lastTimeout = System.currentTimeMillis();

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
					NioServerDefinition config = server.getKey();

					switch (server.getValue())
					{
						case SERVER_ADD:
							addServer(config);
							break;
						case SERVER_REMOVE:
							removeServer(config);
							break;
						case SERVER_UPDATE:
							updateServer(config);
							break;
					}
				}

			}
			catch (CancelledKeyException ex)
			{
				//TODO say how much we really don't care
			}
			catch (IOException ex)
			{
				if (ex instanceof IOException && ex.getMessage().equals("Connection reset by peer"))
				{
					//TODO do the disconnection stuff
				}
			}
			catch (Exception ex)
			{
				LOG.warn("There was an exception while executing in the Socket Server", ex);
			}
		}
	}

	public void addServer(NioServerDefinition server) throws IOException
	{
		ServerSocketChannel serv = ServerSocketChannel.open();
		serv.configureBlocking(false);
		serv.bind(new InetSocketAddress(server.getPort()));
		SelectionKey key = serv.register(channels, SelectionKey.OP_ACCEPT);
		key.attach(server);

		servers.put(server.getId(), key);
	}

	public void removeServer(NioServerDefinition server) throws IOException
	{
		servers.get(server.getId()).channel().close();
		servers.remove(server.getId());
	}

	public void updateServer(NioServerDefinition server)
	{
		SelectionKey key = servers.get(server.getId());
		NioServerDefinition current = (NioServerDefinition) key.attachment();

		current.update(server);
		//TODO if a paticular kind of exception is thrown here, restart the server as it means that the properties cannot be updated without a restart
	}
}
