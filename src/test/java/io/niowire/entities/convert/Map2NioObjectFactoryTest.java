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
package io.niowire.entities.convert;

import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectFactory;
import io.niowire.serializer.LineSerializer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Map2NioObjectFactory}
 *
 * @author Trent Houliston
 */
public class Map2NioObjectFactoryTest
{

	/**
	 * Test that NioObjectFactorys work properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testMap2NioObjectFactory() throws Exception
	{
		Map2NioObjectFactory converter = new Map2NioObjectFactory();

		HashMap<String, Object> map = new HashMap<String, Object>(2);
		map.put("class", "io.niowire.serializer.LineSerializer");
		map.put("configuration", Collections.singletonMap("charset", "utf-8"));

		NioObjectFactory<?> factory = converter.convert(map, NioObjectFactory.class);
		LineSerializer lineslr = (LineSerializer) factory.create();

		assertEquals(LineSerializer.class, lineslr.getClass());

		//Test that the serializer is using utf-8
		String testString = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";

		//Serialize a packet
		lineslr.serialize(new NioPacket("Test", testString));

		//Check that there is now data
		assertTrue("There should be data now that it was serialized", lineslr.hasData());

		//Check that the data is what we sent in utf-8 (to check that the config is passed down)
		ByteBuffer buff = ByteBuffer.allocate((testString + "\n").getBytes("utf-8").length);
		lineslr.read(buff);
		assertFalse("There should be no more data", lineslr.hasData());
		assertArrayEquals("The strings were not equal", buff.array(), (testString + "\n").getBytes("utf-8"));

		//Test that an error is thrown when we don't provide a class
		try
		{
			converter.convert(Collections.EMPTY_MAP, NioObjectFactory.class);
			fail();
		}
		catch (UnconvertableObjectException ex)
		{
			assertNotNull(ex);
		}
	}
}
