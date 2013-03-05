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
package io.niowire.testutilities;

import com.google.gson.Gson;
import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection;
import java.io.File;
import java.lang.reflect.Constructor;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * This class holds utility classes for generating common mock objects which are
 * used through multiple test cases
 *
 * @author Trent Houliston
 */
public class TestUtilities
{

	/**
	 * The default server id that will be used in created objects
	 */
	public static final String DEFAULT_SERVER_ID = "TEST_SERVER_ID";
	/**
	 * The default server name that will be used in created objects
	 */
	public static final String DEFAULT_SERVER_NAME = "TEST_SERVER_NAME";
	/**
	 * The default server port that will be used in created objects
	 */
	public static final int DEFAULT_SERVER_PORT = 0;
	/**
	 * A packet which is used to represent normal input
	 */
	public static final NioPacket BASIC_PACKET = new NioPacket("TEST_PACKET", "BASIC_PACKET");
	/**
	 * A packet which is used to represent a packet which has been modified by
	 * the inspector
	 */
	public static final NioPacket MODIFIED_PACKET = new NioPacket("TEST_PACKET", "MODIFIED_PACKET");
	/**
	 * A packet which is used to represent a packet which would fail
	 * authentication
	 */
	public static final NioPacket FAIL_AUTH_PACKET = new NioPacket("TEST_PACKET", "FAIL_AUTH_PACKET");

	/**
	 * This method creates an Object factory which simply returns the passed
	 * object. And when checking for ownership checks it is the same object that
	 * was passed in.
	 *
	 * @param <T>    the type of the object this factory is for
	 * @param object the object we are passing
	 *
	 * @return a mock of a factory which will "create" this object
	 *
	 * @throws Exception
	 */
	public static <T> NioObjectFactory<T> mockNioObjectFactory(T object) throws Exception
	{
		@SuppressWarnings("unchecked")
		NioObjectFactory<T> f = mock(NioObjectFactory.class);

		//When we mock return the passed object
		when(f.create()).then(new CreateInstanceAnswer<T>(object));
		when(f.create(anyMapOf(String.class, Object.class))).then(new CreateInstanceAnswer<T>(object));

		//When we check for isInstance check its the right object
		when(f.isInstance(Mockito.<T>anyObject())).then(new IsInstanceAnswer<T>(object));

		//Return our mock
		return f;
	}

	/**
	 * Run the noargs constructor of this class (regardless of access level).
	 * Used to run utility class constructors (for code coverage)
	 *
	 * @param clazz the class to run the constructor on
	 *
	 * @throws Exception
	 */
	public static void runPrivateConstructor(Class<?> clazz) throws Exception
	{
		//Get the constructor, set it to accessable and run it
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * This helper method takes a class and finds the json file from this
	 * directory from the class. It then builds the instance, checks it is the
	 * correct class type and returns the object for further testing.
	 *
	 * @param <T>     the type of the object that should be returned
	 * @param clazz   the class object for this object type
	 * @param context the context to attempt to inject into this object
	 *
	 * @return a created instance of the object from the JSON file
	 *
	 * @throws NioObjectCreationException
	 */
	@SuppressWarnings("unchecked")
	public static <T> T buildAndTestFromJson(Class<T> clazz, NioConnection.Context context) throws NioObjectCreationException
	{
		try
		{
			//Read the test json file into the class factory
			Gson g = new Gson();
			String input = new Scanner(clazz.getResourceAsStream(clazz.getSimpleName() + ".json")).useDelimiter("\\Z").next();
			NioObjectFactory<?> factory = g.fromJson(input, NioObjectFactory.class);

			//Create an instance and inject the context
			Object obj = factory.create(Collections.singletonMap("context", context));

			//Check that the object is of the correct type
			assertEquals("The object was not the correct instance", obj.getClass(), clazz);

			return (T) obj;
		}
		catch (NullPointerException ex)
		{
			throw new RuntimeException("The Json file for this class did not exist at "
									   + clazz.getName().replace('.', File.separatorChar)
									   + ".json");
		}
		catch (RuntimeException ex)
		{
			throw ex;
		}
	}

	/**
	 * This method takes a byte array and fills it with random a-z characters
	 *
	 * @param inputArray the array to fill
	 *
	 * @return the array filled with random a-z characters
	 */
	public static byte[] randomCharFill(byte[] inputArray)
	{
		//Get a new random number generator
		Random r = new Random();

		//Loop through the array
		for (int i = 0; i < inputArray.length; i++)
		{
			//Set the value to a random character between a-z
			inputArray[i] = (byte) (r.nextInt(26) + 'a');
		}

		//Return the array
		return inputArray;
	}

	/**
	 * This answer implementation is used to perform the isInstance method of
	 * the factory. It does an instance comparison (checks that the instance it
	 * was constructed with and the passed instance are the same object.
	 *
	 * @param <T>
	 */
	private static class IsInstanceAnswer<T> implements Answer<Boolean>
	{

		private final T object;

		/**
		 * Constructs the answer, Stores the object
		 *
		 * @param object
		 */
		private IsInstanceAnswer(T object)
		{
			this.object = object;
		}

		/**
		 * Check if the passed object is the instance we were constructed with
		 *
		 * @param invocation the invocation details
		 *
		 * @return if the passed object in the invocation is the object we were
		 *         constructed with
		 *
		 * @throws Throwable
		 */
		@Override
		public Boolean answer(InvocationOnMock invocation) throws Throwable
		{
			return this.object == invocation.getArguments()[0];
		}
	}

	/**
	 * This answer constructs a new instance of the object when it's constructor
	 * is called. It will return the object that it was constructed with.
	 *
	 * @param <T> the type of object that this will create
	 */
	private static class CreateInstanceAnswer<T> implements Answer<T>
	{

		private final T object;

		/**
		 * Store the object
		 *
		 * @param object the object to return when we "create"
		 */
		private CreateInstanceAnswer(T object)
		{
			this.object = object;
		}

		/**
		 * Return the object we were created with
		 */
		@Override
		public T answer(InvocationOnMock invocation) throws Throwable
		{
			return object;
		}
	}
}
