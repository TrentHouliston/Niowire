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

import io.niowire.testutilities.RunPrivateUtilityConstructor;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Number2Primitive}
 *
 * @author Trent Houliston
 */
public class Number2PrimitiveTest
{

	/**
	 * Tests converting numbers to other numbers (as primitives)
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNumber2Primitive() throws Exception
	{
		int in = 5;
		double dbl = 4.5;
		long ovr = 123456789123456789L;

		Number result;
		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers into other numbers">
		result = Number2Primitive.convert(in, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 5) == result.byteValue());
		result = Number2Primitive.convert(in, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 5) == result.shortValue());
		result = Number2Primitive.convert(in, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 5 == result.intValue());
		result = Number2Primitive.convert(in, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 5) == result.longValue());
		result = Number2Primitive.convert(in, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 5) == result.floatValue());
		result = Number2Primitive.convert(in, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) 5) == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers into other numbers">
		result = Number2Primitive.convert(dbl, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 4.5) == result.byteValue());
		result = Number2Primitive.convert(dbl, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 4.5) == result.shortValue());
		result = Number2Primitive.convert(dbl, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) 4.5) == result.intValue());
		result = Number2Primitive.convert(dbl, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 4.5) == result.longValue());
		result = Number2Primitive.convert(dbl, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 4.5) == result.floatValue());
		result = Number2Primitive.convert(dbl, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 4.5 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting numbers which overflow into other numbers">
		result = Number2Primitive.convert(ovr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) ovr) == result.byteValue());
		result = Number2Primitive.convert(ovr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) ovr) == result.shortValue());
		result = Number2Primitive.convert(ovr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) ovr) == result.intValue());
		result = Number2Primitive.convert(ovr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ovr == result.longValue());
		result = Number2Primitive.convert(ovr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) ovr) == result.floatValue());
		result = Number2Primitive.convert(ovr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) ovr) == result.doubleValue());
		//</editor-fold>

		//Test that trying to convert to a boolean class tells it to try next converter
		try
		{
			Number2Primitive.convert(in, boolean.class);
			fail();
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}

		//Run the private constructor (for code coverage results)
		RunPrivateUtilityConstructor.runConstructor(Number2Primitive.class);
	}
}
