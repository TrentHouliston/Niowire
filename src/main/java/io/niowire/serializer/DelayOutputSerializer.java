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
package io.niowire.serializer;

import io.niowire.data.NioPacket;
import io.niowire.entities.Initialize;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This serializer is used to delay any packets which are sent to the output
 * from being sent back to the client for the configured amount of time
 * (defaults to 1 second)
 *
 * @author Trent Houliston
 */
public class DelayOutputSerializer implements NioSerializer
{

	private static final Logger LOG = LoggerFactory.getLogger(DelayOutputSerializer.class);
	protected static final DelayQueue<DelayedNioPacket> queue = new DelayQueue<DelayedNioPacket>();
	@Inject
	@Named("serializer")
	NioObjectFactory<NioSerializer> factory;
	@Inject
	protected int delay = 1000;
	@Inject
	protected NioConnection.Context context;
	protected NioSerializer serializer;

	/**
	 * Builds our delay polling thread, as it is a daemon thread it will die
	 * when the app does
	 */
	static
	{
		new DelayThread().start();
	}

	/**
	 * Builds our internal serializer
	 *
	 * @throws NioObjectCreationException
	 * @throws ClassNotFoundException
	 */
	@Initialize
	protected void setup() throws NioObjectCreationException, ClassNotFoundException
	{
		//Build the internal serializer
		serializer = factory.create(Collections.singletonMap("context", context));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NioPacket> deserialize(ByteBuffer buffer) throws IOException
	{
		return serializer.deserialize(buffer);
	}

	/**
	 * This method rather then directly sending the data to be serialized, puts
	 * it into the queue to wait. Once the time has expired it will then be
	 * serialized to be send to the client.
	 */
	@Override
	public void serialize(NioPacket packet) throws IOException
	{
		queue.add(new DelayedNioPacket(delay, packet));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer buffer) throws IOException
	{
		return serializer.read(buffer);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasData() throws IOException
	{
		return serializer.hasData();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebuffer(ByteBuffer data) throws IOException
	{
		serializer.rebuffer(data);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		serializer.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return serializer.isOpen();
	}

	/**
	 * This class is a simple object which holds a packet and delays its release
	 * for the passed time.
	 */
	protected class DelayedNioPacket implements Delayed
	{

		private final long delay;
		private final NioPacket packet;

		/**
		 * This constructs a new delayed packet which waits delay milliseconds
		 * and then is released.
		 *
		 * @param delay  the amount of time in milliseconds to wait from now to
		 *                     release the packet
		 * @param packet the packet to be released at the end of the time
		 */
		protected DelayedNioPacket(long delay, NioPacket packet)
		{
			this.delay = System.currentTimeMillis() + delay;
			this.packet = packet;
		}

		/**
		 * This method is called on the object to send the packet onward to the
		 * serializer
		 */
		protected void send()
		{
			try
			{
				serializer.serialize(packet);
			}
			catch (IOException ex)
			{
				LOG.warn("There was an exception while sending a delayed message", ex);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public long getDelay(TimeUnit unit)
		{
			return unit.convert(delay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int compareTo(Delayed o)
		{
			return Long.compare(this.getDelay(TimeUnit.MILLISECONDS), o.getDelay(TimeUnit.MILLISECONDS));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 53 * hash + (int) (this.delay ^ (this.delay >>> 32));
			hash = 53 * hash + (this.packet != null ? this.packet.hashCode() : 0);
			return hash;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (obj == null)
			{
				return false;
			}
			if (getClass() != obj.getClass())
			{
				return false;
			}
			final DelayedNioPacket other = (DelayedNioPacket) obj;
			if (this.delay != other.delay)
			{
				return false;
			}
			if (this.packet != other.packet && (this.packet == null || !this.packet.equals(other.packet)))
			{
				return false;
			}
			return true;
		}
	}

	/**
	 * This is a daemon thread which listens to all of the packets in all the
	 * delay queues. It will get any packet which has had its timeout expire and
	 * it will send it on through to the serializer that the packet came from.
	 */
	private static class DelayThread extends Thread
	{

		/**
		 * Build our DelayThread
		 */
		private DelayThread()
		{
			this.setDaemon(true);
		}

		/**
		 * This thread simply waits on the queue and sends any packets that are
		 * received onward to the serializer that created them.
		 */
		@Override
		public void run()
		{
			while (true)
			{
				try
				{
					queue.take().send();
				}
				catch (InterruptedException ex)
				{
				}
			}
		}
	}
}
