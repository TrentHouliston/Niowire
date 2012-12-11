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
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * This class is the most basic serializer. It scans the input stream for a
 * particular byte (or set of bytes) and removes them from the stream, and uses
 * all the data up to that point as it's data. This class may be overridden and
 * the serializeBlob() method can be used to take the delimited output to be
 * further processed.
 *
 * Note that this class as it uses a static byte buffer for its operations, is
 * not thread safe across instances. Only one instance of this class can be
 * performing operations at any one time. In the context of Niowire as there is
 * one main thread doing all of the IO this should not be a problem. But if it
 * is going to be used elsewhere this must be considered.
 *
 * @author Trent Houliston
 */
public abstract class DelimitedSerializer implements NioSerializer
{

	/**
	 * This is the static byte buffer shared between all instances of the
	 * serializer.
	 */
	private static final ByteBuffer tb = ByteBuffer.allocateDirect(32768);
	//This buffer is allocated as needed if there is any leftover data (split packets)
	private ByteBuffer residual = null;
	private boolean open = true;
	protected Context context;
	private Queue<ByteBuffer> sendQueue = new LinkedList<ByteBuffer>();
	private ByteBuffer rebuffer = null;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<NioPacket> deserialize(ByteBuffer buffer) throws IOException
	{
		//Check if the channel is closed
		if (!open)
		{
			throw new ClosedChannelException();
		}

		//Our start point is initially 0
		int startPoint = 0;

		//Create a new list to hold the packets we find
		LinkedList<NioPacket> packets = new LinkedList<NioPacket>();

		//The delimiter is removed from the stream and used to break up packets
		byte[] delimiter = getDelimiter();

		//This is how far seraching the delimiter we are
		int depth = 0;

		//Clear the buffer ready for putting data into
		tb.clear();

		//If we have residual from the last run then add it to the transient buffer
		if (residual != null && residual.hasRemaining())
		{
			//Our start point is initially
			startPoint = residual.remaining();
			tb.put(residual);
			residual.clear();
		}

		//Add our data into the buffer as well and get it ready for reading
		tb.put(buffer);
		tb.flip();

		//We will start searching after our residual from last time (we didn't find it there)
		//We will go back far enough to ensure that we will catch partial delimiters
		tb.position(startPoint - delimiter.length < 0 ? 0 : startPoint - delimiter.length);

		//This is our last delimiter we found
		int lastDelimiter = 0;

		//While we have data
		while (tb.hasRemaining())
		{
			//Get a byte
			byte b = tb.get();

			//If this is the start of a delimiter
			if (delimiter[depth] == b)
			{
				//Start searching the next byte next time
				depth++;

				//If we have found the full delimiter
				if (depth == delimiter.length)
				{
					//Get another view of this buffer
					ByteBuffer data = tb.duplicate();

					//Set its start to where we left off
					data.position(lastDelimiter);

					//Set its limit to before the delimiter
					data.limit(tb.position() - delimiter.length);

					//Deserialize this section
					packets.addAll(deserializeBlob(data));

					//Set our delimiter for next time we go through
					lastDelimiter = tb.position();
					depth = 0;
				}
			}
			//If this isn't what we are looking for, reset our search
			else
			{
				depth = 0;
			}
		}

		//If after our whole search, we still have some data left over
		if (tb.limit() > lastDelimiter)
		{
			//Set the buffers position to the position of the last delimiter
			tb.position(lastDelimiter);

			//Work out how much data we need
			int requiredSize = tb.remaining();

			//If we don't have enough
			if (residual == null || residual.capacity() < requiredSize)
			{
				//Get enough to hold it
				residual = ByteBuffer.allocate(requiredSize);
			}

			//Store it for next time
			residual.clear();
			residual.put(tb);
			residual.flip();
		}
		//If we didn't have any data left over, get rid of our residual buffer
		else
		{
			residual = null;
		}

		//Return the packets that we got from this
		return packets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(NioPacket packet) throws IOException
	{
		//Check if the channel is closed
		if (!open)
		{
			throw new ClosedChannelException();
		}

		//Serialize our packets into byte buffers
		ByteBuffer buff = serializeBlob(packet);

		//Add these buffers to the queue
		sendQueue.add(buff);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		//Clear our variables
		sendQueue = null;
		residual = null;
		open = false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer buffer)
	{
		int read = 0;

		//If we have a rebuffer then add it first
		if (rebuffer != null)
		{
			read += rebuffer.remaining();
			buffer.put(rebuffer);
			rebuffer = null;
		}

		//Read as many of our buffers into the passed buffer as we can
		while (!sendQueue.isEmpty()
			   && buffer.remaining() >= sendQueue.peek().remaining() + getDelimiter().length)
		{
			ByteBuffer bb = sendQueue.poll();
			read += bb.remaining();
			read += getDelimiter().length;
			buffer.put(bb);
			buffer.put(getDelimiter());
		}

		//Read as much of our remaining buffer as we can
		if (!sendQueue.isEmpty())
		{
			//Get our next buffer in the queue
			ByteBuffer peek = sendQueue.peek();

			//Note that we don't remove the element here, even if it is empty
			//This is so a delimiter can be added next time (even if there is no data)

			//Create a view of our buffer
			ByteBuffer view = peek.duplicate();

			//Set it's limit to how much free space we have in the buffer
			view.limit(peek.position() + buffer.remaining());

			//Move the position of our byte buffer so that we skip over the bytes we are writing
			peek.position(peek.position() + buffer.remaining());

			//Put as much as we can into the buffer
			buffer.put(view);
		}

		return read;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return open;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContext(Context context)
	{
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasData()
	{
		return !sendQueue.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebuffer(ByteBuffer in)
	{
		//Get the byte buffer
		rebuffer = ByteBuffer.allocate(in.remaining());
		rebuffer.put(in);
		rebuffer.flip();
	}

	protected abstract List<NioPacket> deserializeBlob(ByteBuffer blob) throws IOException;

	protected abstract ByteBuffer serializeBlob(NioPacket packet) throws IOException;

	protected abstract byte[] getDelimiter();
}
