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
import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.util.List;

/**
 * Classes implementing this interface are responsible for converting data from
 * their bytes into Objects, and also for converting Objects into their byte
 * form. This class is also responsible for caching partial bytes which do not
 * yet create a full object, and caching bytes which have not yet been sent to
 * the client.
 *
 * The caching for the data to be sent may either be cached as the object
 * packets, or cached in their byte form.
 *
 * @author Trent Houliston
 */
public interface NioSerializer extends Closeable, ReadableByteChannel
{

	/**
	 * Deserialize the passed byte buffer into Object Packets, the serializer is
	 * responsible for buffering any unused data to be used next time.
	 *
	 * @param buffer the buffer containing the data to be deserialized
	 *
	 * @return a list of ObjectPackets created during the serialization. This
	 *            method should always return a list, even if there are no
	 *            results this run. In such case it is expected to return an
	 *            empty list (e.g. Collections.emptyList())
	 *
	 * @throws IOException if there was an IOException while deserializing
	 */
	public List<NioPacket> deserialize(ByteBuffer buffer) throws IOException;

	/**
	 * This method is responsible for converting object packets into their byte
	 * form. It is also responsible for buffering the bytes of the conversion
	 * until such a time as they can be read (maybe partially) out from the
	 * socket.
	 *
	 * @param packet the packets to be serialized
	 *
	 * @throws IOException if there was an IOException while serializing
	 */
	public void serialize(NioPacket packet) throws IOException;

	/**
	 * Reading from this Serializer means to read out from the bytes which are
	 * waiting to be written to the socket. Objects which have been serialized
	 * through the serialize method should be read out into this buffer.
	 *
	 * @param buffer the buffer to read into
	 *
	 * @return the number of bytes read
	 *
	 * @throws IOException if the serializer is closed
	 */
	@Override
	public int read(ByteBuffer buffer) throws IOException;

	/**
	 * This method returns true when this serializer has data which has been
	 * buffered into it to be written to the client.
	 *
	 * @return true if there is data to be written, false otherwise
	 *
	 * @throws IOException if the serializer is closed
	 */
	public boolean hasData() throws IOException;

	/**
	 * This method is used to send data back that could not be dealt with at a
	 * the socket level to the serializer to be buffered (so all buffering
	 * happens at the same place)
	 *
	 * @param data the {@link ByteBuffer} who's data to store
	 *
	 * @throws IOException if the serializer is closed
	 */
	public void rebuffer(ByteBuffer data) throws IOException;
}
