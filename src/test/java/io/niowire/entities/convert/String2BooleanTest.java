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
 * Unit test for {@link String2Boolean}
 *
 * @author Trent Houliston
 */
public class String2BooleanTest
{

	@Test(timeout = 1000)
	public void testString2Boolean() throws Exception
	{
		//Make our converter
		String2Boolean converter = new String2Boolean();

		//Test true
		assertTrue(converter.convert("True", Boolean.class));
		assertTrue(converter.convert("true", Boolean.class));

		//Test false
		assertFalse(converter.convert("False", Boolean.class));
		assertFalse(converter.convert("false", Boolean.class));
	}
}
