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
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Unit test for {@link Number2Number}
 *
 * @author Trent Houliston
 */
@RunWith(Enclosed.class)
public class Number2NumberTest
{

	/**
	 * This class is for running the parameterized tests
	 */
	@RunWith(Parameterized.class)
	public static class Parametrized
	{

		@Parameterized.Parameter(0)
		public Number in;
		@Parameterized.Parameter(1)
		public Long expectedInt;
		@Parameterized.Parameter(2)
		public Double expectedDouble;

		/**
		 * Tests that the Number2Number converter works as expected
		 *
		 * @throws Exception
		 */
		@Test(timeout = 1000)
		public void testNumber2Number() throws Exception
		{
			//Make our converter
			Number2Number converter = new Number2Number();

			Number result;
			//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers into other numbers">
			result = converter.convert(in, Byte.class);
			assertTrue("The result was not of the correct type", result instanceof Byte);
			assertTrue("The result did not parse to the correct value", expectedInt.byteValue() == result.byteValue());
			result = converter.convert(in, Short.class);
			assertTrue("The result was not of the correct type", result instanceof Short);
			assertTrue("The result did not parse to the correct value", expectedInt.shortValue() == result.shortValue());
			result = converter.convert(in, Integer.class);
			assertTrue("The result was not of the correct type", result instanceof Integer);
			assertTrue("The result did not parse to the correct value", expectedInt.intValue() == result.intValue());
			result = converter.convert(in, Long.class);
			assertTrue("The result was not of the correct type", result instanceof Long);
			assertTrue("The result did not parse to the correct value", expectedInt.longValue() == result.longValue());
			result = converter.convert(in, Float.class);
			assertTrue("The result was not of the correct type", result instanceof Float);
			assertTrue("The result did not parse to the correct value", expectedDouble.floatValue() == result.floatValue());
			result = converter.convert(in, Double.class);
			assertTrue("The result was not of the correct type", result instanceof Double);
			assertTrue("The result did not parse to the correct value", expectedDouble.doubleValue() == result.doubleValue());
			//</editor-fold>
		}

		/**
		 * Gets a list of tests and expected results to run
		 *
		 * @return a list of tests and expected results to run
		 */
		@Parameterized.Parameters
		public static List<?> parameters()
		{
			//Test that numbers convert properly, format is {String}, {Expected Integer}, {Expected Floating Point}
			return Arrays.asList(new Object[][]
					{
						//Test Number
						{
							10, 10L, 10d
						},
						//Test Negative Number
						{
							1.53d, 1L, 1.53d
						}
					});
		}
	}

	/**
	 * This is for Unparameterized tests
	 */
	public static class Unparameterized
	{

		/**
		 * Test that when unsupported number types (like BigDecimal) are put in
		 * it fails
		 */
		@Test(timeout = 1000)
		public void testUnsupportedNumbers()
		{
			Number2Number converter = new Number2Number();
			//Test that unsupported number classes throw errors
			try
			{
				converter.convert(5, BigDecimal.class);
				fail();
			}
			catch (TryNextConverterException ex)
			{
				assertNotNull(ex);
			}
		}
	}
}
