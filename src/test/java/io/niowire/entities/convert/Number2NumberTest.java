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

import java.math.BigDecimal;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Number2Number}
 *
 * @author Trent Houliston
 */
public class Number2NumberTest
{

	/**
	 * Tests that the Number2Number converter works as expected
	 *
	 * @throws Exception
	 */
	@Test
	public void testNumber2Number() throws Exception
	{
		//Make our converter
		Number2Number converter = new Number2Number();

		int in = 5;
		double dbl = 4.5;

		Number result;
		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers into other numbers">
		result = converter.convert(in, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 5) == result.byteValue());
		result = converter.convert(in, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 5) == result.shortValue());
		result = converter.convert(in, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 5 == result.intValue());
		result = converter.convert(in, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 5) == result.longValue());
		result = converter.convert(in, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 5) == result.floatValue());
		result = converter.convert(in, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) 5) == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers into other numbers">
		result = converter.convert(dbl, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 4.5) == result.byteValue());
		result = converter.convert(dbl, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 4.5) == result.shortValue());
		result = converter.convert(dbl, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) 4.5) == result.intValue());
		result = converter.convert(dbl, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 4.5) == result.longValue());
		result = converter.convert(dbl, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 4.5) == result.floatValue());
		result = converter.convert(dbl, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 4.5 == result.doubleValue());
		//</editor-fold>

		//Test that unsupported number classes throw errors
		try
		{
			converter.convert(in, BigDecimal.class);
			fail();
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}
	}
}
