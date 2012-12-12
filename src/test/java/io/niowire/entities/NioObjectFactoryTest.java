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
public class NioObjectFactoryTest
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
		NioObjectFactory<NioObjectImpl> factory = new NioObjectFactory<NioObjectImpl>(NioObjectImpl.class.getName(), Collections.EMPTY_MAP);

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
			NioObjectFactory<NioObjectImpl> factory = new NioObjectFactory<NioObjectImpl>(NioObjectImpl.class.getName(), Collections.singletonMap("Key", new Object()));

			//Try to make a new object (should fail since the map contains something)
			//This was set up in the Impl class
			NioObjectImpl obj = factory.create();

			//If we reached here, an exception was not thrown. Fail the test.
			fail("An NioObjectCreationException should have been thrown during configuration");
		}
		catch (NioObjectCreationException ex)
		{
		}
	}

	/**
	 * This class is a class which is used to make a basic NioObject to use
	 */
	public static class NioObjectImpl implements NioObject
	{

		/**
		 * Configure the object. If the map is not empty then this will throw an
		 * exception (to simulate an error during configuration)
		 *
		 * @param configuration either an empty or non empty configuration
		 *
		 * @throws Exception if the configuration is not empty
		 */
		@Override
		public void configure(Map<String, Object> configuration) throws Exception
		{
			//If they give us a non empty configuration use this as a sign to throw an exception
			if (!configuration.isEmpty())
			{
				throw new Exception();
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
