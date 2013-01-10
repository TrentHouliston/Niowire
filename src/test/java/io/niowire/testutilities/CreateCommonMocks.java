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
import io.niowire.entities.NioObject;
import io.niowire.entities.NioObjectFactory;
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

	public static final String DEFAULT_SERVER_ID = "TEST_SERVER_ID";
	public static final String DEFAULT_SERVER_NAME = "TEST_SERVER_NAME";
	public static final int DEFAULT_SERVER_PORT = 0;
	public static final NioPacket BASIC_PACKET = new NioPacket("TEST_PACKET", "BASIC_PACKET");
	public static final NioPacket MODIFIED_PACKET = new NioPacket("TEST_PACKET", "MODIFIED_PACKET");
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
	public static <T extends NioObject> NioObjectFactory<T> mockNioObjectFactory(T object) throws Exception
	{
		@SuppressWarnings("unchecked")
		NioObjectFactory<T> f = mock(NioObjectFactory.class);

		//When we mock return the passed object
		when(f.create()).then(new CreateInstanceAnswer<T>(object));

		//When we check for isInstance check its the right object
		when(f.isInstance(Mockito.<T>anyObject())).then(new IsInstanceAnswer<T>(object));

		//Return our mock
		return f;
	}

	private static class IsInstanceAnswer<T> implements Answer<Boolean>
	{

		private final T object;

		private IsInstanceAnswer(T object)
		{
			this.object = object;
		}

		@Override
		public Boolean answer(InvocationOnMock invocation) throws Throwable
		{
			return this.object == invocation.getArguments()[0];
		}
	}

	private static class CreateInstanceAnswer<T> implements Answer<T>
	{

		private final T object;

		private CreateInstanceAnswer(T object)
		{
			this.object = object;
		}

		@Override
		public T answer(InvocationOnMock invocation) throws Throwable
		{
			return object;
		}
	}

	public static class ReturnParametersAnswer<T> implements Answer<T>
	{

		@Override
		@SuppressWarnings("unchecked")
		public T answer(InvocationOnMock invocation) throws Throwable
		{
			return (T) invocation.getArguments()[0];
		}
	}
}
