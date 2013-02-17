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
 * Unit test for {@link String2Boolean}
 *
 * @author Trent Houliston
 */
@RunWith(Parameterized.class)
public class String2BooleanTest
{

	@Parameterized.Parameter(0)
	public String str;
	@Parameterized.Parameter(1)
	public Boolean bool;

	/**
	 * Tests that the conversion from the string to boolean works properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString2Boolean() throws Exception
	{
		//Make our converter
		String2Boolean converter = new String2Boolean();

		//Test the value
		assertEquals("The value did not convert properly", converter.convert(str, Boolean.class), bool);
	}

	/**
	 * Gets a list of tests and expected results to run
	 *
	 * @return a list of tests and expected results to run
	 */
	@Parameterized.Parameters
	public static List<?> parameters()
	{
		//Test that true and false convert properly and they are case insensitive
		return Arrays.asList(new Object[][]
				{
					{
						"true", true
					},
					{
						"tRuE", true
					},
					{
						"false", false
					},
					{
						"fAlSe", false
					}
				});
	}
}
