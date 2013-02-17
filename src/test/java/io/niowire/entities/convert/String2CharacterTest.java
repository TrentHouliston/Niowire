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
 * This converter converts String classes to a single character
 *
 * @author Trent Houliston
 */
@RunWith(Parameterized.class)
public class String2CharacterTest
{

	@Parameterized.Parameter(0)
	public String str;
	@Parameterized.Parameter(1)
	public Character ch;

	/**
	 * Check the converter works properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString2Character() throws Exception
	{
		//Make a converter
		String2Character converter = new String2Character();

		//Test converting our string
		assertEquals(ch, converter.convert(str, Character.class));
	}

	/**
	 * Gets a list of tests and expected results to run
	 *
	 * @return a list of tests and expected results to run
	 */
	@Parameterized.Parameters
	public static List<?> parameters()
	{
		//Test that characters convert properly
		return Arrays.asList(new Object[][]
				{
					//Test Character
					{
						"A", 'A'
					},
					//Test String
					{
						"Dogs", 'D'
					},
					//Test Unicode
					{
						"ß", 'ß'
					},
					//Test empty string
					{
						"", null
					}
				});
	}
}
