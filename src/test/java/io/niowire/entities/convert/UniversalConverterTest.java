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

import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectFactory;
import io.niowire.serializer.LineSerializer;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.HashMap;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link UniversalConverter}
 *
 * This test doubles up on the individual converter tests in many cases. This is
 * in order to test that the selection of converters is correct.
 *
 * @author Trent Houliston
 */
public class UniversalConverterTest
{

	/**
	 * Tests that converting objects to Strings uses the toString method and
	 * works properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testString() throws Exception
	{
		Object obj = new Object();
		String str = "Hello World";
		int i = 5;

		assertEquals(obj.toString(), UniversalConverter.doConvert(obj, String.class));
		assertEquals(str, UniversalConverter.doConvert(str, String.class));
		assertEquals(Integer.toString(i), UniversalConverter.doConvert(i, String.class));
	}

	/**
	 * Tests that strings are converted into booleans properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testBooleans() throws Exception
	{
		String tr = "true";
		String fa = "false";

		assertEquals(true, UniversalConverter.doConvert(tr, Boolean.class));
		assertEquals(true, UniversalConverter.doConvert(tr, boolean.class));
		assertEquals(false, UniversalConverter.doConvert(fa, Boolean.class));
		assertEquals(false, UniversalConverter.doConvert(fa, boolean.class));
	}

	/**
	 * Tests that numbers are converted from strings and from other numbers
	 * properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNumbers() throws Exception
	{
		String intStr = "1";
		String dblStr = "0.5";
		String negIntStr = "-5";
		String negDblStr = "-.1";
		int in = 5;
		double dbl = 4.5;
		long ovr = 123456789123456789L;

		Number result;

		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers from Strings">

		result = UniversalConverter.doConvert(intStr, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 1) == result.byteValue());
		result = UniversalConverter.doConvert(intStr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 1) == result.byteValue());
		result = UniversalConverter.doConvert(intStr, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 1) == result.shortValue());
		result = UniversalConverter.doConvert(intStr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 1) == result.shortValue());
		result = UniversalConverter.doConvert(intStr, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 1 == result.intValue());
		result = UniversalConverter.doConvert(intStr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 1 == result.intValue());
		result = UniversalConverter.doConvert(intStr, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 1) == result.longValue());
		result = UniversalConverter.doConvert(intStr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 1) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers from Strings">
		result = UniversalConverter.doConvert(dblStr, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 0.5) == result.floatValue());
		result = UniversalConverter.doConvert(dblStr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 0.5) == result.floatValue());
		result = UniversalConverter.doConvert(dblStr, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 0.5 == result.doubleValue());
		result = UniversalConverter.doConvert(dblStr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 0.5 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative descrete numbers from Strings">
		result = UniversalConverter.doConvert(negIntStr, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) -5) == result.byteValue());
		result = UniversalConverter.doConvert(negIntStr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) -5) == result.byteValue());
		result = UniversalConverter.doConvert(negIntStr, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) -5) == result.shortValue());
		result = UniversalConverter.doConvert(negIntStr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) -5) == result.shortValue());
		result = UniversalConverter.doConvert(negIntStr, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", -5 == result.intValue());
		result = UniversalConverter.doConvert(negIntStr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", -5 == result.intValue());
		result = UniversalConverter.doConvert(negIntStr, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) -5) == result.longValue());
		result = UniversalConverter.doConvert(negIntStr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) -5) == result.longValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting negative floating point numbers from Strings">
		result = UniversalConverter.doConvert(negDblStr, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) -0.1) == result.floatValue());
		result = UniversalConverter.doConvert(negDblStr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) -0.1) == result.floatValue());
		result = UniversalConverter.doConvert(negDblStr, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", -0.1 == result.doubleValue());
		result = UniversalConverter.doConvert(negDblStr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", -0.1 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting discrete numbers into other numbers">
		result = UniversalConverter.doConvert(in, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 5) == result.byteValue());
		result = UniversalConverter.doConvert(in, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 5) == result.byteValue());
		result = UniversalConverter.doConvert(in, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 5) == result.shortValue());
		result = UniversalConverter.doConvert(in, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 5) == result.shortValue());
		result = UniversalConverter.doConvert(in, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 5 == result.intValue());
		result = UniversalConverter.doConvert(in, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", 5 == result.intValue());
		result = UniversalConverter.doConvert(in, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 5) == result.longValue());
		result = UniversalConverter.doConvert(in, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 5) == result.longValue());
		result = UniversalConverter.doConvert(in, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 5) == result.floatValue());
		result = UniversalConverter.doConvert(in, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 5) == result.floatValue());
		result = UniversalConverter.doConvert(in, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) 5) == result.doubleValue());
		result = UniversalConverter.doConvert(in, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) 5) == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting floating point numbers into other numbers">
		result = UniversalConverter.doConvert(dbl, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 4.5) == result.byteValue());
		result = UniversalConverter.doConvert(dbl, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) 4.5) == result.byteValue());
		result = UniversalConverter.doConvert(dbl, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 4.5) == result.shortValue());
		result = UniversalConverter.doConvert(dbl, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) 4.5) == result.shortValue());
		result = UniversalConverter.doConvert(dbl, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) 4.5) == result.intValue());
		result = UniversalConverter.doConvert(dbl, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) 4.5) == result.intValue());
		result = UniversalConverter.doConvert(dbl, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 4.5) == result.longValue());
		result = UniversalConverter.doConvert(dbl, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ((long) 4.5) == result.longValue());
		result = UniversalConverter.doConvert(dbl, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 4.5) == result.floatValue());
		result = UniversalConverter.doConvert(dbl, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) 4.5) == result.floatValue());
		result = UniversalConverter.doConvert(dbl, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 4.5 == result.doubleValue());
		result = UniversalConverter.doConvert(dbl, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", 4.5 == result.doubleValue());
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Test converting numbers which overflow into other numbers">
		result = UniversalConverter.doConvert(ovr, Byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) ovr) == result.byteValue());
		result = UniversalConverter.doConvert(ovr, byte.class);
		assertTrue("The result was not of the correct type", result instanceof Byte);
		assertTrue("The result did not parse to the correct value", ((byte) ovr) == result.byteValue());
		result = UniversalConverter.doConvert(ovr, Short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) ovr) == result.shortValue());
		result = UniversalConverter.doConvert(ovr, short.class);
		assertTrue("The result was not of the correct type", result instanceof Short);
		assertTrue("The result did not parse to the correct value", ((short) ovr) == result.shortValue());
		result = UniversalConverter.doConvert(ovr, Integer.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) ovr) == result.intValue());
		result = UniversalConverter.doConvert(ovr, int.class);
		assertTrue("The result was not of the correct type", result instanceof Integer);
		assertTrue("The result did not parse to the correct value", ((int) ovr) == result.intValue());
		result = UniversalConverter.doConvert(ovr, Long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ovr == result.longValue());
		result = UniversalConverter.doConvert(ovr, long.class);
		assertTrue("The result was not of the correct type", result instanceof Long);
		assertTrue("The result did not parse to the correct value", ovr == result.longValue());
		result = UniversalConverter.doConvert(ovr, Float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) ovr) == result.floatValue());
		result = UniversalConverter.doConvert(ovr, float.class);
		assertTrue("The result was not of the correct type", result instanceof Float);
		assertTrue("The result did not parse to the correct value", ((float) ovr) == result.floatValue());
		result = UniversalConverter.doConvert(ovr, Double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) ovr) == result.doubleValue());
		result = UniversalConverter.doConvert(ovr, double.class);
		assertTrue("The result was not of the correct type", result instanceof Double);
		assertTrue("The result did not parse to the correct value", ((double) ovr) == result.doubleValue());
		//</editor-fold>
	}

	/**
	 * Tests that converting to Char objects is handled correctly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testChar() throws Exception
	{
		String a = "Hello World";
		String b = "x";

		assertEquals("The char did not convert properly", 'H', (char) UniversalConverter.doConvert(a, char.class));
		assertEquals("The char did not convert properly", 'H', (char) UniversalConverter.doConvert(a, Character.class));

		assertEquals("The char did not convert properly", 'x', (char) UniversalConverter.doConvert(b, char.class));
		assertEquals("The char did not convert properly", 'x', (char) UniversalConverter.doConvert(b, Character.class));
	}

	/**
	 * Tests that converting to class objects works as expected (tests the
	 * forName method in {@link Object2Object})
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testClasses() throws Exception
	{
		String test1 = Object.class.getName();
		String test2 = String.class.getName();
		String test3 = UniversalConverter.class.getName();

		assertEquals(Object.class, UniversalConverter.doConvert(test1, Class.class));
		assertEquals(String.class, UniversalConverter.doConvert(test2, Class.class));
		assertEquals(UniversalConverter.class, UniversalConverter.doConvert(test3, Class.class));
	}

	/**
	 * Tests that Maps which are being converted into NioObjectFactorys are
	 * created correctly, and the objects they create work correctly.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNioObjectFactories() throws Exception
	{
		HashMap<String, Object> map = new HashMap<String, Object>(2);
		map.put("class", "io.niowire.serializer.LineSerializer");
		map.put("configuration", Collections.singletonMap("charset", "utf-8"));

		NioObjectFactory<?> factory = UniversalConverter.doConvert(map, NioObjectFactory.class);
		LineSerializer lineslr = (LineSerializer) factory.create();

		assertEquals(LineSerializer.class, lineslr.getClass());

		//Test that the serializer is using utf-8
		String testString = "✓✓Ï‹¸¸Ó´¯¸˘˘°ﬁ·˝∏ÇÏÍÇ¸π“£¢ªº√∆Ωç˚œæ";

		//Serialize a packet
		lineslr.serialize(new NioPacket("Test", testString));

		//Check that there is now data
		assertTrue("There should be data now that it was serialized", lineslr.hasData());

		//Check that the data is what we sent in utf-8 (to check that the config is passed down)
		ByteBuffer buff = ByteBuffer.allocate((testString + "\n").getBytes("utf-8").length);
		lineslr.read(buff);
		assertFalse("There should be no more data", lineslr.hasData());
		assertArrayEquals("The strings were not equal", buff.array(), (testString + "\n").getBytes("utf-8"));
	}

	/**
	 * This method tests if objects that can be casted get returned (same
	 * object) casted to the correct type.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testCasting() throws Exception
	{
		//Make some test data
		String test = "Hello";
		Object converted = UniversalConverter.doConvert(test, Object.class);

		//Check it's the same object
		assertTrue("The same object should have been returned (as it's castable)", test == converted);
	}

	/**
	 * This method tests that nulls are returned as nulls
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNulls() throws Exception
	{
		assertNull(UniversalConverter.doConvert(null, String.class));
		assertNull(UniversalConverter.doConvert(null, Object.class));
	}

	/**
	 * Tests that converters at the same distance are selected properly.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	@SuppressWarnings(
	{
		"unchecked", "unchecked", "unchecked", "unchecked"
	})
	public void testSameLevelConverters() throws Exception
	{
		//Make a local converter for testing
		UniversalConverter converter = new UniversalConverter();

		//Mock some converters
		Converter<TestA, TestB> a2b = mock(Converter.class);
		Converter<Object, TestB> x2b = mock(Converter.class);
		Converter<TestB, TestA> b2a = mock(Converter.class);
		Converter<TestB, TestA> b2a2 = mock(Converter.class);

		//None of them will be adaptive
		when(a2b.isAdaptive()).thenReturn(false);
		when(x2b.isAdaptive()).thenReturn(false);
		when(b2a.isAdaptive()).thenReturn(false);
		when(b2a2.isAdaptive()).thenReturn(false);

		//Set our conversion to classes
		when(a2b.getFrom()).thenReturn(TestA.class);
		when(x2b.getFrom()).thenReturn(Object.class);
		when(b2a.getFrom()).thenReturn(TestB.class);
		when(b2a2.getFrom()).thenReturn(TestB.class);

		//Set our conversion from classes
		when(a2b.getTo()).thenReturn(TestB.class);
		when(x2b.getTo()).thenReturn(TestB.class);
		when(b2a.getTo()).thenReturn(TestA.class);
		when(b2a2.getTo()).thenReturn(TestA.class);

		//Set our conversions
		when(a2b.convert(any(TestA.class), any(Class.class))).thenReturn(new TestB());
		when(x2b.convert(anyObject(), any(Class.class))).thenReturn(new TestB());
		when(b2a.convert(any(TestB.class), any(Class.class))).thenReturn(new TestA());
		when(b2a2.convert(any(TestB.class), any(Class.class))).thenReturn(new TestA());

		//Add in our test converters
		converter.addConverter(a2b);
		converter.addConverter(x2b);
		converter.addConverter(b2a);
		converter.addConverter(b2a2);

		//Run some conversions and make sure the correct stubs worked
		converter.convert(new TestA(), TestB.class);
		verify(a2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(x2b, times(0)).convert(any(TestA.class), any(Class.class));
		verify(b2a, times(0)).convert(any(TestB.class), any(Class.class));
		verify(b2a2, times(0)).convert(any(TestB.class), any(Class.class));

		//Try the second converter
		converter.convert(new Object(), TestB.class);
		verify(a2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(x2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(b2a, times(0)).convert(any(TestB.class), any(Class.class));
		verify(b2a2, times(0)).convert(any(TestB.class), any(Class.class));

		//Try the third converter
		converter.convert(new TestB(), TestA.class);
		verify(a2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(x2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(b2a, times(1)).convert(any(TestB.class), any(Class.class));
		verify(b2a2, times(0)).convert(any(TestB.class), any(Class.class));

		//Modify the 3rd converter to throw an exception
		when(b2a.convert(any(TestB.class), any(Class.class))).thenThrow(new TryNextConverterException());

		//Check that this made it pass on to converter 4
		converter.convert(new TestB(), TestA.class);
		verify(a2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(x2b, times(1)).convert(any(TestA.class), any(Class.class));
		verify(b2a, times(2)).convert(any(TestB.class), any(Class.class));
		verify(b2a2, times(1)).convert(any(TestB.class), any(Class.class));
	}

	/**
	 * Empty class for testing ordering
	 */
	public static class TestA
	{
	}

	/**
	 * Empty class for testing ordering
	 */
	public static class TestB
	{
	}
}
