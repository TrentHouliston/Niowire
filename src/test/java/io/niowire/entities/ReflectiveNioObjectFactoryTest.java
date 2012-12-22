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
package io.niowire.entities;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 *
 * @author trent
 */
public class ReflectiveNioObjectFactoryTest
{

	/**
	 * Test that objects can be created from their class name and a
	 * configuration
	 *
	 * @throws Exception
	 */
	@Test
	public void testObjectCreation() throws Exception
	{
		//Build a new factory
		ReflectiveNioObjectFactory<NioObjectImpl> factory = new ReflectiveNioObjectFactory<NioObjectImpl>(NioObjectImpl.class.getName(), Collections.EMPTY_MAP);

		//Try to create an object
		NioObjectImpl obj = factory.create();

		//Check the object is of the correct type
		assertTrue(obj instanceof NioObjectImpl);
	}

	/**
	 * Test that if there is an error setting up/configuring the object a
	 * wrapped exception is thrown
	 *
	 * @throws Exception ex
	 */
	@Test
	public void testInvalidObjectCreationExceptionWrapping() throws Exception
	{
		try
		{
			//Make a new object
			ReflectiveNioObjectFactory<NioObjectImpl> factory = new ReflectiveNioObjectFactory<NioObjectImpl>(NioObjectImpl.class.getName(), Collections.singletonMap("exception", new Object()));

			//Try to make a new object (should fail since the map contains something)
			//This was set up in the Impl class
			factory.create();

			//If we reached here, an exception was not thrown. Fail the test.
			fail("An NioObjectCreationException should have been thrown during configuration");
		}
		catch (NioObjectCreationException ex)
		{
		}
	}

	/**
	 * Test that if we try to construct something that's not a NioObject it will
	 * fail
	 */
	@Test
	public void testNonNioObjectCreation()
	{
		try
		{
			//Make a new object
			ReflectiveNioObjectFactory<NioObject> factory = new ReflectiveNioObjectFactory<NioObject>("java.lang.String", Collections.singletonMap("foo", new Object()));
			factory.create();
			fail("An exception should have been thrown as String is not a NioObject");
		}
		catch (NioObjectCreationException ex)
		{
			//Make sure that the exception that was thrown was a real exception and not just a re wrap
			assertTrue(ex.getCause() == null);
		}
	}

	/**
	 * Test that runtime exceptions are wrapped when they are thrown into
	 * checked exceptions of type {@link NioObjectCreationException}
	 */
	@Test
	public void testRuntimeExceptionsAreWrapped()
	{
		try
		{
			//Make a new object
			ReflectiveNioObjectFactory<NioObject> factory = new ReflectiveNioObjectFactory<NioObject>(NioObjectImpl.class.getName(), Collections.singletonMap("runtime", new Object()));
			factory.create();
			fail("An exception should have been thrown here (a wrapped runtime exception)");
		}
		catch (NioObjectCreationException ex)
		{
			//Check the cause was a runtime exception
			assertTrue(ex.getCause() instanceof RuntimeException);
		}
	}

	/**
	 * This class is a class which is used to make a basic NioObject to use
	 */
	public static class NioObjectImpl implements NioObject
	{

		/**
		 * Configure the object. If the map contains the key "exception" then it
		 * will throw an {@link Exception}, if it contains the key "runtime" it
		 * will throw a {@link RuntimeException}
		 *
		 * @param configuration a configuration containing either exception,
		 *                            runtime or nothing.
		 *
		 * @throws Exception        if the configuration has the "exception" key
		 * @throws RuntimeException if the configuration has the "runtime" key
		 */
		@Override
		public void configure(Map<String, Object> configuration) throws Exception
		{
			//If they give us an exception key throw an exception
			if (configuration.containsKey("exception"))
			{
				throw new Exception();
			}
			//If they give us a runtime key then throw a runtime exception
			if (configuration.containsKey("runtime"))
			{
				throw new RuntimeException();
			}
		}

		/**
		 * We don't need to do anything for close
		 *
		 * @throws IOException
		 */
		@Override
		public void close() throws IOException
		{
			//Do nothing
		}
	}
}