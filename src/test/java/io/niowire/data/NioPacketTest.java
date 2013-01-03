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
package io.niowire.data;

import java.lang.reflect.Field;
import java.util.Arrays;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NioPacket}
 *
 * @author Trent Houliston
 */
public class NioPacketTest
{

	private static final String SOURCE = "Test";
	private static final String DATA = "Data";

	/**
	 * Tests the basic get methods perform as expected
	 */
	@Test
	public void testPacket()
	{
		//Create our packet and store the creation time
		NioPacket packet = new NioPacket(SOURCE, DATA);
		long creationTime = System.currentTimeMillis();

		//Make sure the construction was done properly
		assertEquals("The source should be the entered source", packet.getSource(), SOURCE);
		assertEquals("The data should be the entered data", packet.getData(), DATA);

		//Change the data
		packet.setData("Data2");

		//Check that the data was updated
		assertEquals("The data should be the entered data", packet.getData(), "Data2");

		//Check that the timestamp is within 1 millisecond
		assertTrue("The timestamps were wrong (or too far off)", (creationTime - packet.getTimestamp()) <= 1);
	}

	/**
	 * Tests the equals method compares objects correctly
	 */
	@Test
	public void testEquals()
	{
		//Create three packets
		NioPacket a = new NioPacket("x", "y");
		NioPacket b = new NioPacket("y", "z");
		NioPacket c = new NioPacket("x", "y");

		//Check the first packet
		assertFalse("These packets should not be equal", a.equals(b));
		assertTrue("These packets should be equal", a.equals(c));

		//Check the second packet
		assertFalse("These packets should not be equal", b.equals(a));
		assertFalse("These packets should not be equal", b.equals(c));

		//Check the third packet
		assertTrue("These packets should be equal", c.equals(a));
		assertFalse("These packets should not be equal", c.equals(b));

		//Check that comparing to a random object fails
		assertFalse("These packets should not be equal and should not throw an exception", a.equals(new Object()));
	}

	/**
	 * Tests that comparing the object to another object orders them by time
	 *
	 * @throws Exception
	 */
	@Test
	public void testCompareTo() throws Exception
	{
		//Set the length we are testing
		int arrayLength = 50;

		//Get the timestamp field using reflection and make it editable
		Field timestamp = NioPacket.class.getDeclaredField("timestamp");
		timestamp.setAccessible(true);

		//Make two arrays
		NioPacket[] packets = new NioPacket[arrayLength];
		long[] times = new long[arrayLength];

		for (int i = 0; i < arrayLength; i++)
		{
			//Create a random number and a packet
			times[i] = (long) (Math.random() * Long.MAX_VALUE);
			packets[i] = new NioPacket(SOURCE, DATA);

			//Set the timestamp in the packet to this time
			timestamp.set(packets[i], times[i]);
		}

		//Sort both arrays
		Arrays.sort(times);
		Arrays.sort(packets);

		//Loop through the array
		for (int i = 0; i < arrayLength; i++)
		{
			//Check the arrays are equal
			assertEquals("The resulting array was not in the correct order", times[i], packets[i].getTimestamp());
		}

		//Check that the lowest element is first (the earliest timestamp)
		assertTrue("The smallest timestamp should be the first element", times[0] < times[arrayLength - 1]);
	}
}
