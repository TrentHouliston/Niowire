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

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * This converter converts String classes to a single character
 *
 * @author Trent Houliston
 */
public class String2CharacterTest
{

	/**
	 * Check the converter works properly
	 *
	 * @throws Exception
	 */
	@Test
	public void testString2Character() throws Exception
	{
		//Make a converter
		String2Character converter = new String2Character();

		//Try converting a few Strings
		assertEquals(Character.valueOf('A'), converter.convert("A", Character.class));
		assertEquals(Character.valueOf('D'), converter.convert("Dogs", Character.class));

		//Blank strings convert to null
		assertNull(converter.convert("", Character.class));
	}
}
