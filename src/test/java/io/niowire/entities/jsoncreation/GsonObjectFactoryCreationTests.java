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
package io.niowire.entities.jsoncreation;

import com.google.gson.Gson;
import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.inspection.TimeoutInspector;
import io.niowire.serializer.DelayOutputSerializer;
import io.niowire.serializer.JsonSerializer;
import io.niowire.serializer.LineSerializer;
import io.niowire.serializer.NioSerializer;
import io.niowire.server.NioConnection.Context;
import io.niowire.serversource.DirectoryServerSource;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.service.EchoService;
import io.niowire.service.NioService;
import java.io.File;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This class contains unit tests to ensure that each of the objects which could
 * exist in the system can be properly created from deserialization from json.
 *
 * @author Trent Houliston
 */
public class GsonObjectFactoryCreationTests
{

	/**
	 * Test that NioServerDefinitions are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioServerDefinition() throws Exception
	{
		Context context = mock(Context.class);
		NioServerDefinition test = buildAndTestInstance(NioServerDefinition.class, context);

		//Test that all the correct data and factories are in the test object
		assertNull(test.getId());
		assertEquals("test", test.getName());
		assertEquals(12012, (int) test.getPort());

		//Test the factories
		NioObjectFactory<? extends NioSerializer> serializerFactory = test.getSerializerFactory();
		NioSerializer serializer = serializerFactory.create();
		assertEquals(serializer.getClass(), LineSerializer.class);
		NioObjectFactory<? extends NioInspector> inspectorFactory = test.getInspectorFactory();
		NioInspector inspector = inspectorFactory.create();
		assertEquals(TimeoutInspector.class, inspector.getClass());
		List<NioObjectFactory<? extends NioService>> serviceFactories = test.getServiceFactories();
		assertEquals(1, serviceFactories.size());
		NioService service = serviceFactories.get(0).create();
		assertEquals(EchoService.class, service.getClass());
	}

	/**
	 * Test that TimeoutInspectors are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testTimeoutInspector() throws Exception
	{
		Context context = mock(Context.class);
		TimeoutInspector test = buildAndTestInstance(TimeoutInspector.class, context);

		//Test that the TimeoutInspector works properly
		test.inspect(new NioPacket("test", "test"));

		assertFalse(test.timeout());
		Thread.sleep(110);
		assertTrue(test.timeout());
	}

	/**
	 * Test that DelayOutputSerializers are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testDelayOutputSerializer() throws Exception
	{
		Context context = mock(Context.class);
		DelayOutputSerializer test = buildAndTestInstance(DelayOutputSerializer.class, context);

		String testString = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";

		//Test that the packets are delayed 100ms

		//Start our clock
		long start = System.currentTimeMillis();

		//Serialize a packet
		test.serialize(new NioPacket("Test", testString));

		//Check that there is no data for 95ms
		while (System.currentTimeMillis() - start < 95)
		{
			assertFalse(test.hasData());
			Thread.sleep(1);
		}

		//Wait 20ms extra (to be sure)
		Thread.sleep(20);

		//Check that there is now data
		assertTrue("Test there is no data", test.hasData());

		//Check that the data is what we sent in utf-8 (to check that the config is passed down)
		ByteBuffer buff = ByteBuffer.allocate((testString + "\n").getBytes("utf-8").length);
		test.read(buff);
		assertFalse(test.hasData());
		assertArrayEquals(buff.array(), (testString + "\n").getBytes("utf-8"));
	}

	/**
	 * Test that Json Serializers are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testJsonSerializer() throws Exception
	{
		Context context = mock(Context.class);
		JsonSerializer test = buildAndTestInstance(JsonSerializer.class, context);

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

	/**
	 * Test that Line serializers are created from Json properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testLineSerializer() throws Exception
	{
		Context context = mock(Context.class);
		LineSerializer test = buildAndTestInstance(LineSerializer.class, context);

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

	/**
	 * Test that Directory Server Sources are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testDirectoryServerSource() throws Exception
	{
		Context context = mock(Context.class);
		DirectoryServerSource test = buildAndTestInstance(DirectoryServerSource.class, context);

		//Test that the server source is reading from the test directory
		assertEquals("The directory targeted is wrong", new File("test"), test.directory);
	}

	/**
	 * Test that EchoServices are created from Json properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testEchoService() throws Exception
	{
		Context context = mock(Context.class);
		EchoService test = buildAndTestInstance(EchoService.class, context);

		//Test that any packet we send is sent back
		NioPacket data = new NioPacket("test", "test");

		//Send and check it echoed
		test.send(data);
		verify(context).write(data);
	}

	/**
	 * This helper method takes a class and finds the json file from this
	 * directory from the class. It then builds the instance, checks it is the
	 * correct class type and returns the object for further testing.
	 *
	 * @param <T>     the type of the object that should be returned
	 * @param clazz   the class object for this object type
	 * @param context the context to attempt to inject into this object
	 *
	 * @return a created instance of the object from the JSON file
	 *
	 * @throws NioObjectCreationException
	 */
	@SuppressWarnings("unchecked")
	private <T> T buildAndTestInstance(Class<T> clazz, Context context) throws NioObjectCreationException
	{
		//Read the test json file into the class factory
		Gson g = new Gson();
		String input = new Scanner(GsonObjectFactoryCreationTests.class.getResourceAsStream(clazz.getSimpleName() + ".json")).useDelimiter("\\Z").next();
		NioObjectFactory<?> factory = g.fromJson(input, NioObjectFactory.class);

		//Create an instance and inject the context
		Object obj = factory.create(Collections.singletonMap("context", context));

		//Check that the object is of the correct type
		assertEquals("The object was not the correct instance", obj.getClass(), clazz);

		return (T) obj;
	}
}
