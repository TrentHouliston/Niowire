package io.niowire.serializer;

import io.niowire.data.ObjectPacket;
import io.niowire.entities.NioObject;
import io.niowire.server.NioContextUser;
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
public interface NioSerializer extends NioObject, NioContextUser, ReadableByteChannel
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
	public List<ObjectPacket> deserialize(ByteBuffer buffer) throws IOException;

	/**
	 * This method is responsible for converting object packets into their byte
	 * form. It is also responsible for buffering the bytes of the conversion
	 * until such a time as they can be read (maybe partially) out from the
	 * socket.
	 *
	 * @param packets the packets to be serialized
	 *
	 * @return a ByteBuffer containing the binary representation of the packets
	 *
	 * @throws IOException if there was an IOException while serializing
	 */
	public ByteBuffer serialize(List<ObjectPacket> packets) throws IOException;

	/**
	 * Reading from this Serializer means to read out from the bytes which are
	 * waiting to be written to the socket. Objects which have been serialized
	 * through the serialize method should be read out into this buffer.
	 *
	 * @param buffer the buffer to read into
	 *
	 * @return the number of bytes read
	 */
	@Override
	public int read(ByteBuffer buffer);
}
