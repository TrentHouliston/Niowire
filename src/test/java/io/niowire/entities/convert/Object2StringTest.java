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
 * Unit test for {@link Object2String}
 *
 * @author Trent Houliston
 */
public class Object2StringTest
{

	/**
	 * Tests that objects passed into this method are correctly converted into
	 * Strings using the toString method
	 */
	@Test
	public void testObject2String()
	{
		Object2String converter = new Object2String();

		//Test converting several objects to strings
		Object o = new Object();
		assertEquals("The converter did not convert the object properly", o.toString(), converter.convert(o, String.class));
		assertEquals("The converter did not convert the object properly", "Hello World", converter.convert("Hello World", String.class));
		assertEquals("The converter did not convert the object properly", Integer.valueOf(10).toString(), converter.convert(Integer.valueOf(10), String.class));
		assertEquals("The converter did not convert the object properly", Integer.toString(5), converter.convert(5, String.class));
	}
}
