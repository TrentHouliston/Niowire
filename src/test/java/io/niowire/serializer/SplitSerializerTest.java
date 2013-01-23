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
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection.Context;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link SplitSerializer}
 *
 * @author Trent Houliston
 */
public class SplitSerializerTest
{

	private NioSerializer splitSerializer = null;
	private NioSerializer inputSerializier = null;
	private NioSerializer outputSerializer = null;

	/**
	 * Sets up a split serializer, as well as both the input and output
	 * serializers on their own.
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		//Build a config for each
		Map<String, Object> splitConfig = new HashMap<String, Object>(4);
		Map<String, Object> inputConfig = new HashMap<String, Object>(1);
		Map<String, Object> outputConfig = new HashMap<String, Object>(1);
		inputConfig.put("charset", "utf-8");
		outputConfig.put("charset", "utf-8");
		splitConfig.put("inputClass", JsonSerializer.class);
		splitConfig.put("inputConfiguration", inputConfig);
		splitConfig.put("outputClass", LineSerializer.class);
		splitConfig.put("outputConfiguration", outputConfig);

		//Mock some contexts
		Context context = mock(Context.class);

		//Create factories for each of the objects
		NioObjectFactory<SplitSerializer> splitFactory = new NioObjectFactory<SplitSerializer>(SplitSerializer.class, splitConfig);
		NioObjectFactory<JsonSerializer> inputFactory = new NioObjectFactory<JsonSerializer>(JsonSerializer.class, inputConfig);
		NioObjectFactory<LineSerializer> outputFactory = new NioObjectFactory<LineSerializer>(LineSerializer.class, outputConfig);

		//Inject details
		this.splitSerializer = splitFactory.create(Collections.singletonMap("context", context));
		this.inputSerializier = inputFactory.create(Collections.singletonMap("context", context));
		this.outputSerializer = outputFactory.create(Collections.singletonMap("context", context));
	}

	/**
	 * Cleans up after a test
	 */
	@After
	public void teardown()
	{
		this.splitSerializer = null;
		this.inputSerializier = null;
		this.outputSerializer = null;
	}

	/**
	 * Test that the input object is used correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDataInput() throws Exception
	{
		//Wrap some test data
		ByteBuffer buff = ByteBuffer.wrap("{\'data\':true}\n".getBytes("utf-8"));

		//Parse it in both
		List<NioPacket> split = splitSerializer.deserialize(buff);
		buff.rewind();
		List<NioPacket> input = inputSerializier.deserialize(buff);

		//Check the two packets are equal apart from the data
		assertEquals("There should be the same number of packets in both", split.size(), input.size());
		assertEquals("There should be only one packet", 1, split.size());
		assertEquals("The packets data was wrong", input.get(0).getData(), split.get(0).getData());
	}

	/**
	 * Test that the output object is used correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDataOutput() throws Exception
	{
		//Make packets and serialize them
		NioPacket nioPacket = new NioPacket("Hello", Collections.singletonMap("data", true));
		splitSerializer.serialize(nioPacket);
		outputSerializer.serialize(nioPacket);

		//Read them out
		ByteBuffer split = ByteBuffer.allocate(100);
		ByteBuffer output = ByteBuffer.allocate(100);
		assertEquals("Check they read the same amount", splitSerializer.read(split), outputSerializer.read(output));

		//Check they are the same
		assertArrayEquals("The arrays for these two packets should have been identical", split.array(), output.array());

	}
}
