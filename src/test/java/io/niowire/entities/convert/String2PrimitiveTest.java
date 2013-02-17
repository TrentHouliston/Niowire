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

import io.niowire.testutilities.TestUtilities;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link String2Primitive}
 *
 * @author Trent Houliston
 */
public class String2PrimitiveTest
{

	/**
	 * Tests that Strings are converted to primitives properly through the
	 * {@link String2Primitive} class
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString2Primitive() throws Exception
	{
		String intStr = "1";
		String dblStr = "0.5";
		String negIntStr = "-5";
		String negDblStr = "-.1";

		Number result;

		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers from Strings">
		result = (Number) String2Primitive.convert(intStr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 1) == result.byteValue());
		result = (Number) String2Primitive.convert(intStr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 1) == result.shortValue());
		result = (Number) String2Primitive.convert(intStr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 1 == result.intValue());
		result = (Number) String2Primitive.convert(intStr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 1) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers from Strings">
		result = (Number) String2Primitive.convert(dblStr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 0.5) == result.floatValue());
		result = (Number) String2Primitive.convert(dblStr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 0.5 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative descrete numbers from Strings">
		result = (Number) String2Primitive.convert(negIntStr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) -5) == result.byteValue());
		result = (Number) String2Primitive.convert(negIntStr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) -5) == result.shortValue());
		result = (Number) String2Primitive.convert(negIntStr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", -5 == result.intValue());
		result = (Number) String2Primitive.convert(negIntStr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) -5) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative floating point numbers from Strings">
		result = (Number) String2Primitive.convert(negDblStr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) -0.1) == result.floatValue());
		result = (Number) String2Primitive.convert(negDblStr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", -0.1 == result.doubleValue());
		//</editor-fold>

		//Test booleans
		assertTrue("The result did not parse to the correct value", (Boolean) String2Primitive.convert("true", boolean.class));
		assertFalse("The result did not parse to the correct value", (Boolean) String2Primitive.convert("false", boolean.class));

		//TODO test for chars

		//Try converting a few Strings
		assertEquals(Character.valueOf('A'), String2Primitive.convert("A", char.class));
		assertEquals(Character.valueOf('D'), String2Primitive.convert("Dogs", char.class));

		//Blank String converts to \u0000
		assertEquals(Character.valueOf('\u0000'), String2Primitive.convert("", char.class));

		//Run our utility class builder
		TestUtilities.runPrivateConstructor(String2Primitive.class);
	}
}
