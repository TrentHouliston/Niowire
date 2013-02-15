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

import java.nio.charset.Charset;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link String2Object}
 *
 * @author Trent Houliston
 */
public class String2ObjectTest
{

	/**
	 * Tests that creation of the object by using the forName method works
	 * properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString2Object() throws Exception
	{
		String2Object converter = new String2Object();

		//Attempt converting a few objects
		assertEquals("The object did not convert properly", Object.class, converter.convert(Object.class.getName(), Class.class));
		assertEquals("The object did not convert properly", String.class, converter.convert(String.class.getName(), Class.class));
		assertEquals("The object did not convert properly", UniversalConverter.class, converter.convert(UniversalConverter.class.getName(), Class.class));

		assertEquals("The object did not convert properly", Charset.forName("utf-8"), converter.convert("utf-8", Charset.class));
		assertEquals("The object did not convert properly", Charset.forName("ascii"), converter.convert("ascii", Charset.class));

		//Test a class without a forName method throws an error
		try
		{
			converter.convert("Something", Object.class);
			fail();
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}
	}
}
