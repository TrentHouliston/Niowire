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

import java.io.File;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link Object2Object}
 *
 * @author Trent Houliston
 */
public class Object2ObjectTest
{

	@Test
	public void testObject2Object() throws Exception
	{
		Object2Object converter = new Object2Object();

		//test valueOf method (Integers valueof method will return the same object)
		Object ob1 = converter.convert(new char[]
				{
					'H', 'E', 'L', 'L', 'O'
				}, String.class);
		Object ob2 = converter.convert(1024, String.class);
		assertTrue(ob1 instanceof String);
		assertTrue(ob2 instanceof String);
		assertEquals("HELLO", ob1);
		assertEquals("1024", ob2);

		//Test valueOf which accepts a boxed parameter (but not a primitive)
		assertNull(converter.convert(Integer.valueOf(10), ValueOfTests.class));

		//Test valueOf which throws an Exception
		try
		{
			converter.convert(Integer.valueOf(10), ValueOfTests.class);
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}

		//Test constructors
		Object str = converter.convert("HelloWorld", String.class);
		assertTrue(str instanceof String);
		assertEquals("HelloWorld", str);
		Object file = converter.convert("RandomFile", File.class);
		assertTrue(file instanceof File);
		assertEquals(new File("RandomFile"), file);

		//Test with a primitive parameter
		Object i = converter.convert(3, Integer.class);
		assertTrue(i instanceof Integer);
		assertEquals(3, i);

		//Test constructor which does not exist (for error)
		try
		{
			converter.convert("Anything", NoAvailableConstructorTest.class);
			fail();
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}
	}

	/**
	 * This class holds several valueOf methods used to test various cases
	 */
	public static class ValueOfTests
	{

		/**
		 * This tests cases where a boxed parameter is accepted rather then a
		 * primitive
		 *
		 * @param i the value
		 *
		 * @return null
		 */
		public static ValueOfTests valueOf(Integer i)
		{
			return null;
		}

		/**
		 * This tests cases where the valueOf method throws an exception
		 *
		 * @param s a string
		 *
		 * @return does not return
		 *
		 * @throws Exception always
		 */
		public static ValueOfTests valueOf(String s) throws Exception
		{
			throw new Exception();
		}

		/**
		 * Private constructor
		 */
		private ValueOfTests()
		{
		}
	}

	/**
	 * This class is for testing the case where there is not an available
	 * constructor to use
	 */
	public static class NoAvailableConstructorTest
	{

		/**
		 * An empty constructor
		 */
		public NoAvailableConstructorTest()
		{
		}
	}
}
