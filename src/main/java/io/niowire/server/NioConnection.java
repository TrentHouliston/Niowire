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

import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioAuthenticationException;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.service.NioService;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This Class is responsible for passing the data from a client between the
 * various components, and for returning data to the client. It holds an
 * {@link NioInspector}, {@link NioSerializer} and several {@link NioService}
 * objects. It will read data in from the client, send it through the parser,
 * then through the inspector then to each of the services.
 *
 * Data on the return trip will also be collected from the serializer.
 *
 * @author Trent Houliston
 */
public class NioConnection implements ReadableByteChannel, WritableByteChannel
{

	private static final Logger LOG = LoggerFactory.getLogger(NioConnection.class);
	//Initial Configuration Objects
	private SelectionKey SELECTION_KEY;
	private NioServerDefinition SERVER_CONFIG;
	//Shared context
	private Context context;
	//Parses the binary stream into objects
	private NioSerializer serializer;
	/**
	 * The inspector, (packets are run through this first before being sent to
	 * servers) it can be used for modification of packets, filtering of
	 * packets, or authentication of packets
	 */
	private NioInspector inspect;
	//The services that this connection sends to
	private List<NioService> services;
	//If this connection is open
	private boolean open = true;

	/**
	 * This creates a new NioConnection for a particular server. it handles all
	 * of the data which comes in through a byte buffer. It then uses the
	 * serializer to convert this byte stream into an object stream. This object
	 * stream is then sent through the inspector (for filtering, modification
	 * and authentication of packets) and is then sent through to all of the
	 * services.
	 *
	 * @param key          the selection key that is being used by the Selector
	 *                        to select this channel
	 * @param serverConfig the configuration of the server which contains all
	 *                        the service definitions as well as meta data about
	 *                        the server.
	 *
	 * @throws NioConnectionException if there is a problem setting up the
	 *                                   services
	 */
	public NioConnection(SelectionKey key, NioServerDefinition serverConfig) throws NioConnectionException
	{
		//Store our variables
		this.SELECTION_KEY = key;
		this.SERVER_CONFIG = serverConfig;
		this.services = new ArrayList<NioService>(serverConfig.getServiceFactories().size());
		this.context = new Context();

		try
		{
			//Create our serializer from the definition and pass in our context
			serializer = SERVER_CONFIG.getSerializerFactory().create();
			serializer.setContext(context);
			//Create our inspector from the definition and pass in our context
			inspect = SERVER_CONFIG.getInspectorFactory().create();
			inspect.setContext(context);

			//Create all our services
			for (NioObjectFactory<NioService> factory : SERVER_CONFIG.getServiceFactories())
			{
				try
				{
					//Create this service
					NioService service = factory.create();
					//Set the context to the service
					service.setContext(context);
					services.add(service);
				}
				catch (NioObjectCreationException ex)
				{
					LOG.warn("A service threw an error while attempting to create it", ex);
				}
			}
		}
		//Explicitly catch runtime exception (we want to wrap all exceptions)
		catch (RuntimeException ex)
		{
			//Wrap it in a connection exception and throw it
			throw new NioConnectionException(ex);
		}
		//Catch any exception that might occur
		catch (Exception ex)
		{
			//Wrap it in a connection exception and throw it
			throw new NioConnectionException(ex);
		}
	}

	/**
	 * Updates the interest operations on the SelectionKey, it checks if the
	 * serializer has data to write. And if it does then it updates the
	 * selection key to listen for {@link SelectionKey.OP_WRITE} operations as
	 * well
	 *
	 * @throws IOException
	 */
	public void updateInterestOps() throws IOException
	{
		if (!open)
		{
			throw new ClosedChannelException();
		}

		//Start with the Read operation and add in the write operation if we need to
		SELECTION_KEY.interestOps(SelectionKey.OP_READ | (serializer.hasData() ? SelectionKey.OP_WRITE : 0));
	}

	/**
	 * This method occurs when data is retrieved from the client and is then
	 * sent to this NioConnection for processing. The buffer is deserialized
	 * into an object then filtered and sent to the services.
	 *
	 * @param src the byte buffer source containing the data to be read
	 *
	 * @return the number of bytes written
	 *
	 * @throws ClosedChannelException if the channel is closed
	 * @throws IOException            if the serializer has an IOException on
	 *                                   trying to deserialize
	 */
	@Override
	public int write(ByteBuffer src) throws ClosedChannelException, IOException
	{
		//Check if the connection is open
		if (!open)
		{
			throw new ClosedChannelException();
		}

		//Get the number of remaining bytes (that's how many we will read)
		int bytes = src.remaining();

		//Deserialize the data
		List<NioPacket> packets = serializer.deserialize(src);

		//Loop through all the packets
		for (NioPacket packet : packets)
		{
			try
			{
				NioPacket p = inspect.inspect(packet);

				//Loop through all the services sending the mangled packet
				for (NioService service : services)
				{
					service.send(p);
				}
			}
			//This exception can be thrown by any of the services or the mangler
			catch (NioAuthenticationException ex)
			{
				try
				{
					//Close the channel as it failed authentication
					SELECTION_KEY.channel().close();
				}
				catch (IOException ex1)
				{
					//Log that a connection failed authentication
					LOG.trace("The connection {} failed authentication", this);
				}
			}
		}

		//Return the number of bytes remaining as the serializer should have absorbed them allf
		return bytes;
	}

	/**
	 * This method is used to read as much of the data out of our serializer as
	 * possible into the buffer. This buffer should then be used to send data to
	 * the client.
	 *
	 * @param dst the destination buffer
	 *
	 * @return the number of bytes read
	 *
	 * @throws IOException if the connection has been closed or the serializer
	 *                        has closed
	 */
	@Override
	public int read(ByteBuffer dst) throws IOException
	{
		//Check that we are open
		if (!open)
		{
			throw new ClosedChannelException();
		}

		//Read from the serializer into the destination buffer
		int read = serializer.read(dst);

		//Update our interest ops (we might no longer need to write)
		updateInterestOps();

		return read;
	}

	/**
	 * This method is used to send data which was not yet used by the client
	 * (too much for the socket) back to the connection to be buffered in the
	 * serializer.
	 *
	 * @param buffer the buffer to be re-buffered
	 *
	 * @throws IOException if the serializer is closed
	 */
	public void rebuffer(ByteBuffer buffer) throws IOException
	{
		serializer.rebuffer(buffer);
	}

	/**
	 * Returns if this NioConnection is open.
	 *
	 * @return true if the connection is open, false otherwise
	 */
	@Override
	public boolean isOpen()
	{
		//Return if we are open
		return open;
	}

	/**
	 * Close this NioConnection, release all the resources associated with this
	 * connection and tell all the services, serializer and inspector to also
	 * close (release resources)
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		//Close our channel if it hasn't been closed already
		SELECTION_KEY.channel().close();

		//Tell all our objects that we are using that they should close (clean up)
		serializer.close();
		inspect.close();
		for (NioService service : services)
		{
			service.close();
		}

		//Wipe out variables so they can be garbage collected
		serializer = null;
		inspect = null;
		services = null;
		context = null;

		//We are closed
		open = false;
	}

	/**
	 * This method is called periodically as a way to ask the NioConnection to
	 * check if it should time out. This feature is handled by the inspect
	 * object as it receives every packet which goes through and can make a
	 * decision based on them.
	 *
	 * @throws IOException
	 */
	public void timeout() throws IOException
	{
		//Check the inspect for a timeout
		if (inspect.timeout())
		{
			//Close this connection
			this.close();
		}
	}

	/**
	 * The toString method gets the UID from the inspect object as the toString
	 * of this object.
	 *
	 * @return the String representation of this object (it's UID)
	 */
	@Override
	public String toString()
	{
		return inspect.getUid();
	}

	/**
	 * This class is provided to all of the NioService objects as a way to
	 * interact with the connection and each other. It provides several methods
	 * which can tell the connection to update itself, as well as a map which
	 * can hold properties to be shared by the services.
	 */
	public class Context
	{

		/**
		 * Tell this connection to refresh it's interest operations. This should
		 * be run by a service when it's interest state has change and it now
		 * has data which it needs to write back to the client.
		 *
		 * @throws IOException if the connection is closed
		 */
		public void refreshInterestOps() throws IOException
		{
			//Update our interest ops
			updateInterestOps();
		}

		/**
		 * Writes a packet back to the client
		 *
		 * @param packet the packet to be written
		 *
		 * @throws IOException if the serializer is closed
		 */
		public void write(NioPacket packet) throws IOException
		{
			serializer.serialize(packet);
			refreshInterestOps();
		}

		/**
		 * Gets the InetSocketAddress of the other end of the connection. This
		 * is the IP/Port of the socket which we are connected to.
		 *
		 * @return the InetSocketAddress of the connection we are connected to
		 */
		public InetSocketAddress getRemoteAddress()
		{
			try
			{
				//Get the socket address
				return (InetSocketAddress) ((SocketChannel) SELECTION_KEY.channel()).getRemoteAddress();
			}
			catch (IOException ex)
			{
				//Otherwise return null
				return null;
			}
		}

		/**
		 * Get the UID of this connection (be aware that this can change during
		 * execution based on the inspect object)
		 *
		 * @return the Unique identifier for this connection, used to identify
		 *               packets from it in services.
		 */
		public String getUid()
		{
			return inspect.getUid();
		}

		/**
		 * Gets the ID of the server (a unique string identifying the server
		 * object which this connection came from). (this will not change during
		 * execution)
		 *
		 * @return the unique ID of the server
		 */
		public String getServerId()
		{
			return SERVER_CONFIG.getId();
		}

		/**
		 * Gets the declared name of the server which this connection came from.
		 * (This can change during execution)
		 *
		 * @return the name of the server
		 */
		public String getServerName()
		{
			return SERVER_CONFIG.getName();
		}

		/**
		 * Gets the port of the server we are connecting from. (this will not
		 * change during execution)
		 *
		 * @return the port of the server
		 */
		public int getServerPort()
		{
			return SERVER_CONFIG.getPort();
		}
	}
}
