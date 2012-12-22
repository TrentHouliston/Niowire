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
package io.niowire;

import io.niowire.entities.NioObjectCreationException;
import io.niowire.inspection.NioAuthenticationException;
import io.niowire.serializer.NioInvalidDataException;
import io.niowire.server.NioConnectionException;
import io.niowire.server.NioPropertyUnchangableException;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This test class ensures that all the Throwables have some basic code coverage
 *
 * @author Trent Houliston
 */
public class ThrowablesTest
{

	/**
	 * Private method for testing an Throwable of a paticular class type
	 *
	 * @param clazz the class type to test
	 *
	 * @throws Exception
	 */
	private void testThrowable(Class<? extends Throwable> clazz) throws Exception
	{
		//Build our exceptions
		Throwable ex1 = clazz.getConstructor().newInstance();
		Throwable ex2 = clazz.getConstructor(String.class).newInstance("Test");
		Throwable ex3 = clazz.getConstructor(Throwable.class).newInstance(new TestThrowable("Test"));
		Throwable ex4 = clazz.getConstructor(String.class, Throwable.class).newInstance("Test2", new TestThrowable("Test2"));

		//Check exception 1
		assertNull("The noargs constructor should have a null cause", ex1.getCause());
		assertNull("The noargs constructor should have a null message", ex1.getMessage());

		//Check exception 2
		assertNull("The String constructor should have a null cause", ex2.getCause());
		assertEquals("The String constructor should have a message", "Test", ex2.getMessage());

		//Check exception 3
		assertEquals("The Throwable constructor should have a cause", "Test", ex3.getCause().getMessage());
		assertTrue("The Throwable which is returned should be the TestThrowable", ex3.getCause() instanceof TestThrowable);
		assertTrue("The Throwable constructor should have the causes message as the end of it's message", ex3.getMessage().endsWith(ex3.getCause().getMessage()));

		//Check exception 4
		assertEquals("The String, Throwable constructor should have a cause", "Test2", ex4.getCause().getMessage());
		assertTrue("The Throwable which is returned should be the TestThrowable", ex4.getCause() instanceof TestThrowable);
		assertEquals("The String, Throwable constructor should have a message", "Test2", ex4.getMessage());
	}

	/**
	 * Tests {@link NiowireException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNiowireException() throws Exception
	{
		testThrowable(NiowireException.class);
	}

	/**
	 * Tests {@link RuntimeNiowireException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testRuntimeNiowireException() throws Exception
	{
		testThrowable(RuntimeNiowireException.class);
	}

	/**
	 * Tests {@link NioAuthenticationException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioAuthenticationException() throws Exception
	{
		testThrowable(NioAuthenticationException.class);
	}

	/**
	 * Tests {@link NioConnectionException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioConnectionException() throws Exception
	{
		testThrowable(NioConnectionException.class);
	}

	/**
	 * Tests {@link NioInvalidDataException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioInvalidDataException() throws Exception
	{
		testThrowable(NioInvalidDataException.class);
	}

	/**
	 * Tests {@link NioPropertyUnchangableException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioPropertyUnchangableException() throws Exception
	{
		testThrowable(NioPropertyUnchangableException.class);
	}

	/**
	 * Tests {@link NioObjectCreationException}
	 *
	 * @throws Exception
	 */
	@Test
	public void testNioObjectCreationException() throws Exception
	{
		testThrowable(NioObjectCreationException.class);
	}

	/**
	 * A test throwable for checking that the instanceof is correct for causes
	 */
	private static class TestThrowable extends Throwable
	{

		private static final long serialVersionUID = 1L;

		/**
		 * Create an exception with a string
		 *
		 * @param s the string to create
		 */
		private TestThrowable(String s)
		{
			super(s);
		}
	}
}
