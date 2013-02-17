package io.niowire.serializer;

import io.niowire.data.NioPacket;
import io.niowire.entities.Injector;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection;
import io.niowire.testutilities.TestUtilities;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link JsonSerializer}
 *
 * @author Trent Houliston
 */
public class JsonSerializerTest
{

	private JsonSerializer serializer = null;
	private static final String charset = "utf-8";

	/**
	 * Setup a JSON Serializer
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		//Mock a context
		NioConnection.Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Build our serializer
		serializer = new JsonSerializer();

		//Push through a configuration
		Injector<JsonSerializer> injector = new Injector<JsonSerializer>(JsonSerializer.class, Collections.singletonMap("charset", charset));
		injector.inject(serializer, Collections.singletonMap("context", context));
	}

	/**
	 * Delete the JSON Serializer
	 */
	@After
	public void teardown()
	{
		//Get rid of our variables
		serializer = null;
	}

	/**
	 * Test serialization works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testSerialize() throws Exception
	{
		//Create some test data packets (using a linked map so it maintains order in the output)
		LinkedHashMap<String, Object> testInput = new LinkedHashMap<String, Object>(6);

		//Fill the test data with some stuff
		testInput.put("a", "hello word");
		testInput.put("b", 1);
		testInput.put("c", true);
		testInput.put("d", null);
		testInput.put("e", new Integer[]
				{
					1, 2, 3
				});
		testInput.put("f", Collections.singletonMap("this", "yellow"));

		//Write out our expected string
		String expected = "{\"a\":\"hello word\",\"b\":1,\"c\":true,\"d\":null,\"e\":[1,2,3],\"f\":{\"this\":\"yellow\"}}\n";

		//Serialize the packet
		serializer.serialize(new NioPacket("TEST", testInput));

		//Make a buffer and read into it
		ByteBuffer read = ByteBuffer.allocate(500);
		serializer.read(read);
		read.flip();

		//Decode it using UTF-8
		String result = Charset.forName(charset).decode(read).toString();

		//Check it serialized
		assertEquals("The JSON did not serialize into the expected result", expected, result);
	}

	/**
	 * Test serializing POJOs works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testSerializePojo() throws Exception
	{
		//Create some test data packets (using a linked map so it maintains order in the output)
		JsonSerializerPojoTest input = new JsonSerializerPojoTest();

		//Write out our expected string
		String expected = "{\"dog\":\"woof\",\"cat\":\"meow\"}\n";

		//Serialize the packet
		serializer.serialize(new NioPacket("TEST", input));

		//Make a buffer and read into it
		ByteBuffer read = ByteBuffer.allocate(500);
		serializer.read(read);
		read.flip();

		//Decode it using UTF-8
		String result = Charset.forName(charset).decode(read).toString();

		//Check it serialized
		assertEquals("The JSON did not serialize into the expected result", expected, result);
	}

	/**
	 * Test deserializing into POJOs works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDeserializePojo() throws Exception
	{
		//Write out our input string
		String input = "{\"dog\":\"meow\",\"cat\":\"woof\"}\n";

		HashMap<String, String> newConfig = new HashMap<String, String>(2);
		newConfig.put("charset", charset);
		newConfig.put("pojoClass", JsonSerializerPojoTest.class.getName());

		//Push through a new configuration
		Injector<JsonSerializer> injector = new Injector<JsonSerializer>(JsonSerializer.class, newConfig);
		injector.inject(serializer);

		//Make a buffer
		ByteBuffer buff = ByteBuffer.wrap(input.getBytes(charset));

		//Deserialize our packets
		List<NioPacket> packets = serializer.deserialize(buff);

		assertEquals("There should be 1 packet", 1, packets.size());
		assertTrue("The packet shoudl be our test object", packets.get(0).getData() instanceof JsonSerializerPojoTest);
		assertEquals("The dog should say meow", "meow", ((JsonSerializerPojoTest) packets.get(0).getData()).dog);
		assertEquals("The cat should say woof", "woof", ((JsonSerializerPojoTest) packets.get(0).getData()).cat);
	}

	/**
	 * Test Deserializing into hashmaps works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDeserialize() throws Exception
	{
		//Set up some test data and expected results
		String message = "{'foo':'bar','number':2,'boolean':true,'null':null}\n";
		Map<String, Object> expected = new HashMap<String, Object>(4);
		expected.put("foo", "bar");
		expected.put("number", 2.0);
		expected.put("boolean", true);
		expected.put("null", null);

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertTrue("The wrong data was returned", expected.equals(packets.get(0).getData()));
	}

	/**
	 * Test handling of Unicode characters works as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testUnicodeJsonStrings() throws Exception
	{
		//Create some test data packets (using a linked map so it maintains order in the output)
		Map<String, String> input = Collections.singletonMap("Íˆ©∑¢ªºΩçΩ¸ÇØ˝º©∑ª", "Íˆ©∑¢ªºΩçΩ¸ÇØ˝º©∑ª");

		//Write out our expected string
		String expected = "{\"Íˆ©∑¢ªºΩçΩ¸ÇØ˝º©∑ª\":\"Íˆ©∑¢ªºΩçΩ¸ÇØ˝º©∑ª\"}\n";

		//Serialize the packet
		serializer.serialize(new NioPacket("TEST", input));

		//Make a buffer and read into it
		ByteBuffer read = ByteBuffer.allocate(500);
		serializer.read(read);
		read.flip();

		//Decode it using UTF-8
		String result = Charset.forName(charset).decode(read).toString();

		//Check it serialized
		assertEquals("The JSON did not serialize into the expected result", expected, result);
	}

	/**
	 * Test that a Json array works as expected when serialized
	 */
	@Test(timeout = 1000)
	public void testserializeJsonArray() throws Exception
	{
		//Create some test data packets (using a linked map so it maintains order in the output)
		int[] ints = new int[]
		{
			1, 2, 3, 4, 5, 6, 7, 8, 9
		};

		//Write out our expected string
		String expected = "[1,2,3,4,5,6,7,8,9]\n";

		//Serialize the packet
		serializer.serialize(new NioPacket("TEST", ints));

		//Make a buffer and read into it
		ByteBuffer read = ByteBuffer.allocate(500);
		serializer.read(read);
		read.flip();

		//Decode it using UTF-8
		String result = Charset.forName(charset).decode(read).toString();

		//Check it serialized
		assertEquals("The JSON did not serialize into the expected result", expected, result);
	}

	/**
	 * Test that a Json array works as expected when deserialized
	 */
	@Test(timeout = 1000)
	public void testDeserializeJsonArray() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Set up some test data and expected results
		String message = "['bar', 2.0, true, null]\n";
		List<Object> expected = new LinkedList<Object>();
		expected.add("bar");
		expected.add(2.0);
		expected.add(true);
		expected.add(null);

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure only one packet was returned
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		//Check that the correct data was returned
		assertTrue("The wrong data was returned", expected.equals(packets.get(0).getData()));
	}

	/**
	 * Test that invalid json is wrapped either ignored, or thrown in an
	 * exception
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testInvalidJson() throws Exception
	{
		//Using UTF-8
		String charset = "utf-8";

		//Create some invalid json
		String message = "Totally valid json?\n"
						 + "{'followedby':'validjson'}\n"
						 + "Followed by more invalid json!!1121!\n";

		Map<String, String> expected = Collections.singletonMap("followedby", "validjson");

		//Wrap the message in a buffer
		ByteBuffer data = ByteBuffer.wrap(message.getBytes(charset));

		//Try to deserialize it
		List<NioPacket> packets = serializer.deserialize(data);

		//Make sure that only one packet was returned (the invalid lines got ignored)
		assertEquals("Only a single packet should have been returned", 1, packets.size());

		assertEquals("The returned data was not what was expected", expected, packets.get(0).getData());
	}

	/**
	 * Static class for testing the serializing of POJOs
	 */
	public static class JsonSerializerPojoTest
	{

		public String dog = "woof";
		public String cat = "meow";
	}


	/**
	 * Test that Json Serializers are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testJsonCreation() throws Exception
	{
		NioConnection.Context context = mock(NioConnection.Context.class);
		JsonSerializer test = TestUtilities.buildAndTestFromJson(JsonSerializer.class, context);

		//Test that the serializer is using utf-8
		String testIn = "{'string':'✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ'}\n";
		String expected = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";
		//Test that the packets are delayed 100ms

		//Serialize a packet
		List<NioPacket> packets = test.deserialize(ByteBuffer.wrap(testIn.getBytes("utf-8")));

		assertEquals("There should only be one packet", 1, packets.size());

		assertTrue("The data should be a map", packets.get(0).getData() instanceof Map);

		assertEquals("The data in the packet should be what we gave it", expected, ((Map) packets.get(0).getData()).get("string"));
	}
}