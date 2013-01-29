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

import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectFactory;
import java.util.Map;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.mockito.Mockito.*;

/**
 * This class holds utility classes for generating common mock objects which are
 * used through multiple test cases
 *
 * @author Trent Houliston
 */
public class CreateCommonMocks
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
		 *               constructed with
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

	/**
	 * This is an answer which returns the first parameter it was passed with as
	 * the return.
	 *
	 * @param <T> the type of the object we are returning
	 */
	public static class ReturnParametersAnswer<T> implements Answer<T>
	{

		/**
		 * Return the object we were invoked with
		 *
		 * @param invocation the invocation details
		 *
		 * @return the first parameter of the invocation
		 *
		 * @throws Throwable
		 */
		@Override
		@SuppressWarnings("unchecked")
		public T answer(InvocationOnMock invocation) throws Throwable
		{
			return (T) invocation.getArguments()[0];
		}
	}
}
