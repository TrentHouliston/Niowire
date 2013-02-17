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

import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Object2String}
 *
 * @author Trent Houliston
 */
@RunWith(Parameterized.class)
public class Object2StringTest
{

	@Parameterized.Parameter(0)
	public Object input;
	@Parameterized.Parameter(1)
	public String expected;

	/**
	 * Tests that objects passed into this method are correctly converted into
	 * Strings using the toString method
	 */
	@Test(timeout = 1000)
	public void testObject2String()
	{
		Object2String converter = new Object2String();

		assertEquals("The converter did not convert the object properly", expected, converter.convert(input, String.class));
	}

	/**
	 * Gets a list of tests and expected results to run
	 *
	 * @return a list of tests and expected results to run
	 */
	@Parameterized.Parameters
	public static List<?> parameters()
	{
		Object o = new Object();

		//Test that Strings convert to objects properly (using forName)
		return Arrays.asList(new Object[][]
				{
					//Test object
					{
						o, o.toString()
					},
					{
						"Hello World", "Hello World"
					},
					//Test number
					{
						5, Integer.toString(5)
					},
					//Test char array special case
					{
						new char[]
						{
							'F', 'o', 'o', 'b', 'a', 'r'
						}, "Foobar"
					},
					//Test Character array special case
					{
						new Character[]
						{
							'F', 'o', 'o', 'b', 'a', 'r'
						}, "Foobar"
					}
				});
	}
}
