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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;
import io.niowire.data.ObjectPacket;
import io.niowire.server.NioConnection;
import io.niowire.server.NioConnection.Context;

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
	private ByteBuffer residualRecieve = null;
	private ByteBuffer residualSend = null;
	private boolean open = true;
	protected Context context;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public List<ObjectPacket> deserialize(ByteBuffer buffer) throws IOException
	{
		//TODO throw a closed exception

		//Our start point is initially 0
		int startPoint = 0;

		//Create a new list to hold the packets we find
		LinkedList<ObjectPacket> packets = new LinkedList<ObjectPacket>();

		//The delimiter is removed from the stream and used to break up packets
		byte[] delimiter = getDelimiter();

		//This is how far seraching the delimiter we are
		int depth = 0;

		//Clear the buffer ready for putting data into
		tb.clear();

		//If we have residual from the last run then add it to the transient buffer
		if (residualSend != null && residualSend.hasRemaining())
		{
			//Our start point is initially
			startPoint = residualSend.remaining();
			tb.put(residualSend);
			residualSend.clear();
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
			if (residualSend == null || residualSend.capacity() < requiredSize)
			{
				//Get enough to hold it
				residualSend = ByteBuffer.allocate(requiredSize);
			}

			//Store it for next time
			residualSend.clear();
			residualSend.put(tb);
			residualSend.flip();
		}

		//Return the packets that we got from this
		return packets;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public ByteBuffer serialize(List<ObjectPacket> packets) throws IOException
	{
		ByteBuffer buff = serializeBlob(packets);

		//TODO buffer this buffer so that we only have to send the data that we can, and buffer the rest for next time

		return buff;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		residualSend = null;
		residualRecieve = null;
		open = false;
	}


	@Override
	public int read(ByteBuffer buffer)
	{
		//TODO read our to send data into this buffer
		return 0;
	}

	@Override
	public boolean isOpen()
	{
		//Todo implement a closing things
		return true;
	}

	@Override
	public void setContext(Context context)
	{
		this.context = context;
	}

	protected abstract List<ObjectPacket> deserializeBlob(ByteBuffer blob) throws IOException;

	protected abstract ByteBuffer serializeBlob(List<ObjectPacket> objects) throws IOException;

	protected abstract byte[] getDelimiter();
}
