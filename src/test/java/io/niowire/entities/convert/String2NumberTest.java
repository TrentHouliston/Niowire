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
 * Unit test for {@link String2Number}
 *
 * @author Trent Houliston
 */
public class String2NumberTest
{

	/**
	 * Tests that Strings are converted into numbers correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString2Number() throws Exception
	{
		String2Number converter = new String2Number();
		Number result;

		//Our test Strings
		String intStr = "1";
		String dblStr = "0.5";
		String negIntStr = "-5";
		String negDblStr = "-.1";

		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers from Strings">
		result = converter.convert(intStr, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 1) == result.byteValue());
		result = converter.convert(intStr, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 1) == result.shortValue());
		result = converter.convert(intStr, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 1 == result.intValue());
		result = converter.convert(intStr, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 1) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers from Strings">
		result = converter.convert(dblStr, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 0.5) == result.floatValue());
		result = converter.convert(dblStr, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 0.5 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative descrete numbers from Strings">
		result = converter.convert(negIntStr, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) -5) == result.byteValue());
		result = converter.convert(negIntStr, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) -5) == result.shortValue());
		result = converter.convert(negIntStr, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", -5 == result.intValue());
		result = converter.convert(negIntStr, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) -5) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative floating point numbers from Strings">
		result = converter.convert(negDblStr, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) -0.1) == result.floatValue());
		result = converter.convert(negDblStr, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", -0.1 == result.doubleValue());
		//</editor-fold>

		//Test unsupported numbers
		try
		{
			converter.convert(intStr, BigDecimal.class);
			fail();
		}
		catch (TryNextConverterException ex)
		{
			assertNotNull(ex);
		}
	}
}
