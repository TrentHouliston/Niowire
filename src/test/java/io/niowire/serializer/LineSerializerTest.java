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
import io.niowire.entities.Injector;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection;
import io.niowire.server.NioConnection.Context;
import io.niowire.testutilities.TestUtilities;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CodingErrorAction;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link LineSerializer}
 *
 * @author Trent Houliston
 */
public class LineSerializerTest
{

	/**
	 * Tests that liens are serialized into bytes properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testSerialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		NioPacket message = new NioPacket("TEST", "Hello World, This is a test!");
		byte[] expected = "Hello World, This is a test!\n".getBytes(charset);

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Allocate a buffer to hold the result
		ByteBuffer buffer = ByteBuffer.allocate(100);

		//Serialize the data and read it back from the serializer
		serializer.serialize(message);
		serializer.read(buffer);

		//Get the buffer ready for reading and read the results
		buffer.flip();
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);

		//Make sure that what we got back was what we expected
		assertArrayEquals("The returned data was not the expected result", expected, result);
	}

	/**
	 * Tests that lines are deserialized properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDeserialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "Hello World, This is a test!\n";
		String expected = "Hello World, This is a test!";

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertEquals("The wrong data was returned", expected, packets.get(0).getData());
	}

	/**
	 * Tests that multiple lines which are all injected at once serialize
	 * correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testMultiLineDeserialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "Hello World, This is a test!\n"
						 + "With several lines of data\n"
						 + "Each of these should be a new line\n";
		String[] expected = message.split("\n");

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure three packets were returned
		assertEquals("There should be 3 packets returned", 3, packets.size());

		//Loop through each packet
		for (int i = 0; i < packets.size(); i++)
		{
			//Check that the correct data was returned
			assertEquals("The wrong data was returned for packet " + i, expected[i], packets.get(i).getData());
		}
	}

	/**
	 * Tests that the program can handle input with \r\n or just \n (note that
	 * at present it does not handle just \r)
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testCarriageReturns() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "Hello World, This is a test!\r\n"
						 + "With several lines of data\r\n"
						 + "Each of these should be a new line\r\n";
		String[] expected = message.split("\r\n");

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure three packets were returned
		assertEquals("There should be 3 packets returned", 3, packets.size());

		//Loop through each packet
		for (int i = 0; i < packets.size(); i++)
		{
			//Check that the correct data was returned
			assertEquals("The wrong data was returned for packet " + i, expected[i], packets.get(i).getData());
		}
	}

	/**
	 * Tests that several character sets work with the system
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testCharsets() throws Exception
	{
		String[] charsets = new String[]
		{
			"US-ASCII",
			"utf-8",
			"utf-16be",
			"utf-16le",
			"koi8-r"
		};

		String testString = "The quick brown fox jumps a lazy dog";

		for (String charset : charsets)
		{
			//Mock a context
			Context context = mock(NioConnection.Context.class);
			when(context.getUid()).thenReturn("TEST");

			//Build our serializer
			LineSerializer serializer = new LineSerializer();

			//Push through a configuration
			Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
			injector.inject(serializer, Collections.singletonMap("context", context));

			//Wrap the message in a buffer
			ByteBuffer data = ByteBuffer.wrap((testString + "\n").getBytes(charset));

			//Try to deserialize it
			List<NioPacket> packets = serializer.deserialize(data);

			//Make sure three packets were returned
			assertEquals("There should be 1 packets returned for charset " + charset, 1, packets.size());

			//Check that the correct data was returned
			assertEquals("The wrong data was returned for charset " + charset, testString, packets.get(0).getData());
		}
	}

	/**
	 * Tests serializing Unicode strings works properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testUnicodeSerialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		NioPacket message = new NioPacket("TEST", "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ");
		byte[] expected = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ\n".getBytes(charset);

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Allocate a buffer to hold the result
		ByteBuffer buffer = ByteBuffer.allocate(100);

		//Serialize the data and read it back from the serializer
		serializer.serialize(message);
		serializer.read(buffer);

		//Get the buffer ready for reading and read the results
		buffer.flip();
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);

		//Make sure that what we got back was what we expected
		assertArrayEquals("The returned data was not the expected result", expected, result);
	}

	/**
	 * Tests that deseriaizing Unicode strings behaves properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testUnicodeDeserialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ\n";
		String expected = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertEquals("The wrong data was returned", expected, packets.get(0).getData());
	}

	/**
	 * Tests that when invalid characters are received by the system, it will
	 * handle them properly (by replacing them with the replacement character)
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testInvalidUnicodeCharactersDeserialize() throws Exception
	{
		//We are testing using UTF-8 (other charsets should work the same)
		String charset = "utf-8";

		//Get our test file data
		InputStream testStream = LineSerializer.class.getResourceAsStream("utf-8-decoding-test.txt");
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		//Read it and put it into a byte buffer
		for (int b = testStream.read(); b >= 0; b = testStream.read())
		{
			bos.write(b);
		}
		bos.close();
		testStream.close();
		ByteBuffer input = ByteBuffer.wrap(bos.toByteArray());

		//Get our expected result file data
		String[] expected = new Scanner(LineSerializer.class.getResourceAsStream("utf-8-decoding-test.txt"), charset).useDelimiter("\\Z").next().replaceAll("\r", "").split("\n");

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Get our packets
		List<NioPacket> packets = serializer.deserialize(input);

		for (int i = 0; i < packets.size(); i++)
		{
			//Check that each line is the expected result
			assertEquals(expected[i], packets.get(i).getData());
		}
	}

	/**
	 * Tests that raw data is added to the packets correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testRawDataDeserialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "Hello World, This is a test!\n";
		String expected = "Hello World, This is a test!";
		byte[] expectedRaw = message.getBytes(charset);

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Build a config
		HashMap<String, Object> config = new HashMap<String, Object>(2);
		config.put("charset", charset);
		config.put("raw", true);

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, config);
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertEquals("The wrong data was returned", expected, packets.get(0).getData());
		assertArrayEquals("The raw data returned was not correct", expectedRaw, packets.get(0).getRawData());
		assertTrue("The packet did not report itself as raw", packets.get(0).isRaw());
	}

	/**
	 * Test that when the raw data flag is set on the packet that the serializer
	 * uses the raw data instead of the actual data
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testRawDataSerialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Get some random bytes to test with
		byte[] expected = new byte[100];
		Random r = new Random();
		r.nextBytes(expected);

		//Set up some test data
		NioPacket message = new NioPacket("TEST", null, true, expected);

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Get a config
		HashMap<String, Object> config = new HashMap<String, Object>(2);
		config.put("charset", charset);
		config.put("raw", true);

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, config);
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Allocate a buffer to hold the result
		ByteBuffer buffer = ByteBuffer.allocate(200);

		//Serialize the data and read it back from the serializer
		serializer.serialize(message);
		serializer.read(buffer);

		//Get the buffer ready for reading and read the results
		buffer.flip();
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);

		//Make sure that what we got back was what we expected
		assertArrayEquals("The returned data was not the expected result", expected, result);
	}

	/**
	 * Tests that if the serializer is not setup as a raw serializer, it ignores
	 * raw data
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNonRawOmitsRawSerialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Get some data to test with
		String input = "Hello world!";
		byte[] expected = "Hello world!\n".getBytes(charset);

		//Set up some test data
		NioPacket message = new NioPacket("TEST", input, true, new byte[0]);

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Get a config
		HashMap<String, Object> config = new HashMap<String, Object>(2);
		config.put("charset", charset);
		config.put("raw", false);

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, config);
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Allocate a buffer to hold the result
		ByteBuffer buffer = ByteBuffer.allocate(200);

		//Serialize the data and read it back from the serializer
		serializer.serialize(message);
		serializer.read(buffer);

		//Get the buffer ready for reading and read the results
		buffer.flip();
		byte[] result = new byte[buffer.remaining()];
		buffer.get(result);

		//Make sure that what we got back was what we expected
		assertArrayEquals("The returned data was not the expected result", expected, result);
	}

	/**
	 * Tests that if the serializer is not setup as a raw serializer , it
	 * ignores raw data
	 */
	@Test(timeout = 1000)
	public void testNonRawOmitsRawDeserialize() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "Hello World, This is a test!\n";
		String expected = "Hello World, This is a test!";

		//Mock a context
		Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		LineSerializer serializer = new LineSerializer();

		//Build a config
		HashMap<String, Object> config = new HashMap<String, Object>(2);
		config.put("charset", charset);
		config.put("raw", false);

		//Push through a configuration
		Injector<LineSerializer> injector = new Injector<LineSerializer>(LineSerializer.class, config);
		injector.inject(serializer, Collections.singletonMap("context", context));

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertEquals("The wrong data was returned", expected, packets.get(0).getData());
		assertNull("The raw data was returned when it should not have", packets.get(0).getRawData());
		assertFalse("The packet reported itself as raw", packets.get(0).isRaw());
	}


	/**
	 * Test that Line serializers are created from Json properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testJsonCreation() throws Exception
	{
		Context context = mock(Context.class);
		LineSerializer test = TestUtilities.buildAndTestFromJson(LineSerializer.class, context);

		//Test that the serializer is using utf-8
		String testString = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";

		//Serialize a packet
		test.serialize(new NioPacket("Test", testString));

		//Check that there is now data
		assertTrue("There should be data now that it was serialized", test.hasData());

		//Check that the data is what we sent in utf-8 (to check that the config is passed down)
		ByteBuffer buff = ByteBuffer.allocate((testString + "\n").getBytes("utf-8").length);
		test.read(buff);
		assertFalse("There should be no more data", test.hasData());
		assertArrayEquals("The strings were not equal", buff.array(), (testString + "\n").getBytes("utf-8"));
	}
}