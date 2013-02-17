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
 * Unit test for {@link String2Number}
 *
 * @author Trent Houliston
 */
@RunWith(Enclosed.class)
public class String2NumberTest
{

	/**
	 * This class is for running the parameterized tests
	 */
	@RunWith(Parameterized.class)
	public static class Parametrized
	{

		@Parameterized.Parameter(0)
		public String input;
		@Parameterized.Parameter(1)
		public Long expectedInt;
		@Parameterized.Parameter(2)
		public Double expectedFloat;

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

			if (expectedInt != null)
			{
				result = converter.convert(input, Byte.class);
				assertTrue("The result was not of the correct type", result instanceof Byte);
				assertTrue("The result did not parse to the correct value", expectedInt.byteValue() == result.byteValue());
				result = converter.convert(input, Short.class);
				assertTrue("The result was not of the correct type", result instanceof Short);
				assertTrue("The result did not parse to the correct value", expectedInt.shortValue() == result.shortValue());
				result = converter.convert(input, Integer.class);
				assertTrue("The result was not of the correct type", result instanceof Integer);
				assertTrue("The result did not parse to the correct value", expectedInt.intValue() == result.intValue());
				result = converter.convert(input, Long.class);
				assertTrue("The result was not of the correct type", result instanceof Long);
				assertTrue("The result did not parse to the correct value", expectedInt.longValue() == result.longValue());
			}
			if (expectedFloat != null)
			{
				if (!expectedFloat.isNaN())
				{
					result = converter.convert(input, Float.class);
					assertTrue("The result was not of the correct type", result instanceof Float);
					assertTrue("The result did not parse to the correct value", expectedFloat.floatValue() == result.floatValue());
					result = converter.convert(input, Double.class);
					assertTrue("The result was not of the correct type", result instanceof Double);
					assertTrue("The result did not parse to the correct value", expectedFloat.doubleValue() == result.doubleValue());

				}
				//Special case for NaN (as NaN != NaN)
				else
				{
					result = converter.convert(input, Float.class);
					assertTrue("The result was not of the correct type", result instanceof Float);
					assertTrue("The result did not parse to the correct value", Float.isNaN(result.floatValue()));
					result = converter.convert(input, Double.class);
					assertTrue("The result was not of the correct type", result instanceof Double);
					assertTrue("The result did not parse to the correct value", Double.isNaN(result.doubleValue()));
				}
			}
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
							"1", 1L, 1d
						},
						//Test Negative Number
						{
							"-1", -1L, -1d
						},
						//Test Floating Point Number
						{
							"1.24", 1L, 1.24d
						},
						//Test Negative Floating Point Number
						{
							"-5.61", -5L, -5.61d
						},
						//Test Infinity
						{
							"Infinity", null, Double.POSITIVE_INFINITY
						},
						//Test NaN
						{
							"NaN", null, Double.NaN
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
			String2Number converter = new String2Number();

			//Test unsupported numbers
			try
			{
				converter.convert("5", BigDecimal.class);
				fail();
			}
			catch (TryNextConverterException ex)
			{
				assertNotNull(ex);
			}
		}
	}
}
