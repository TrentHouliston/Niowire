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
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class implements the SplitSerializer, it internally uses two serializers
 * one which is used for data input, and the other which is used for data
 * output.
 *
 * @author Trent Houliston
 */
public class SplitSerializer implements NioSerializer
{

	@Inject
	@Named(value = "inputSerializer")
	private NioObjectFactory<NioSerializer> inputFactory;
	@Inject
	@Named(value = "outputSerializer")
	private NioObjectFactory<NioSerializer> outputFactory;
	@Inject
	protected Context context;
	private NioSerializer input;
	private NioSerializer output;

	/**
	 * Initializes the SplitSerializer, builds both our input and output
	 * serializers
	 *
	 * @throws NioObjectCreationException
	 * @throws IllegalAccessException
	 */
	@Initialize
	public void init() throws NioObjectCreationException, IllegalAccessException
	{
		//Create and inject
		input = inputFactory.create(Collections.singletonMap("context", context));
		output = outputFactory.create(Collections.singletonMap("context", context));
	}

	/**
	 * Deserializes the incoming data using the Input serializer
	 *
	 * @param buffer the buffer to deserialize
	 *
	 * @return a list of packets which have been deserialized
	 *
	 * @throws IOException
	 */
	@Override
	public List<NioPacket> deserialize(ByteBuffer buffer) throws IOException
	{
		return input.deserialize(buffer);
	}

	/**
	 * Serializes the passed packet using the output serializer
	 *
	 * @param packet the packet to be serialized
	 *
	 * @throws IOException
	 */
	@Override
	public void serialize(NioPacket packet) throws IOException
	{
		output.serialize(packet);
	}

	/**
	 * Reads the serialized data from the output serializer
	 *
	 * @param buffer the buffer to read into
	 *
	 * @return the number of bytes read into the buffer
	 *
	 * @throws IOException
	 */
	@Override
	public int read(ByteBuffer buffer) throws IOException
	{
		return output.read(buffer);
	}

	/**
	 * Returns if the output serializer has data
	 *
	 * @return true if the output serializer has data
	 *
	 * @throws IOException
	 */
	@Override
	public boolean hasData() throws IOException
	{
		return output.hasData();
	}

	/**
	 * Passes the passed buffer back to the output serializer to be rebuffered
	 *
	 * @param data the data to be rebuffered
	 *
	 * @throws IOException
	 */
	@Override
	public void rebuffer(ByteBuffer data) throws IOException
	{
		output.rebuffer(data);
	}

	/**
	 * Closes both of the serializers
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		input.close();
		output.close();
	}

	/**
	 * Tests if both of the serializers are open
	 *
	 * @return true if both serializers are open, false otherwise
	 */
	@Override
	public boolean isOpen()
	{
		return input.isOpen() && output.isOpen();
	}
}
