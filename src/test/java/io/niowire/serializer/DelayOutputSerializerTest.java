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
import io.niowire.server.NioConnection.Context;
import io.niowire.testutilities.TestUtilities;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DelayOutputSerializer}
 *
 * @author Trent Houliston
 */
public class DelayOutputSerializerTest
{

	/**
	 * Test that packets on the output are delayed
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDelayedOutput() throws Exception
	{
		//Make a test config
		HashMap<String, Object> config = new HashMap<String, Object>(1);

		//Make a serializer to use inside the delayer
		HashMap<String, Object> serializerConfig = new HashMap<String, Object>(2);
		serializerConfig.put("class", LineSerializer.class);
		serializerConfig.put("configuration", Collections.singletonMap("charset", "utf-8"));

		//Delay of 100ms
		config.put("delay", 100);
		config.put("serializer", serializerConfig);

		//Mock a context
		Context context = mock(Context.class);

		//Create and inject an object
		Injector<DelayOutputSerializer> injector = new Injector<DelayOutputSerializer>(DelayOutputSerializer.class, config);
		DelayOutputSerializer serializer = new DelayOutputSerializer();
		injector.inject(serializer, Collections.singletonMap("context", context));

		String testString = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";
		//Test that the packets are delayed 100ms

		//Start our clock
		long start = System.currentTimeMillis();

		//Serialize a packet
		serializer.serialize(new NioPacket("Test", testString));

		//Check that there is no data for 95ms
		while (System.currentTimeMillis() - start < 95)
		{
			assertFalse(serializer.hasData());
			Thread.sleep(1);
		}

		//Wait 20ms extra (to be sure)
		Thread.sleep(20);

		//Check that there is now data
		assertTrue("Test there is no data", serializer.hasData());

		//Check that the data is what we sent in utf-8 (to check that the config is passed down)
		ByteBuffer buff = ByteBuffer.allocate((testString + "\n").getBytes("utf-8").length);
		serializer.read(buff);
		assertFalse(serializer.hasData());
		assertArrayEquals(buff.array(), (testString + "\n").getBytes("utf-8"));
	}

	/**
	 * Test that DelayOutputSerializers are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testJsonCreation() throws Exception
	{
		Context context = mock(Context.class);
		DelayOutputSerializer test = TestUtilities.buildAndTestFromJson(DelayOutputSerializer.class, context);

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
}
