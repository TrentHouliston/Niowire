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
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Unit test for {@link String2Object}
 *
 * @author Trent Houliston
 */
@RunWith(Parameterized.class)
public class String2ObjectTest
{

	@Parameterized.Parameter(0)
	public String input;
	@Parameterized.Parameter(1)
	public Object result;
	@Parameterized.Parameter(2)
	public Class<?> type;

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
		assertEquals("The object did not convert properly", result, converter.convert(input, type));

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

	/**
	 * Gets a list of tests and expected results to run
	 *
	 * @return a list of tests and expected results to run
	 */
	@Parameterized.Parameters
	public static List<?> parameters()
	{
		//Test that Strings convert to objects properly (using forName)
		return Arrays.asList(new Object[][]
				{
					//Test two classes
					{
						Object.class.getName(), Object.class, Class.class
					},
					{
						String.class.getName(), String.class, Class.class
					},
					//Test two charset objects
					{
						"utf-8", Charset.forName("utf-8"), Charset.class
					},
					{
						"ascii", Charset.forName("ascii"), Charset.class
					}
				});
	}
}
