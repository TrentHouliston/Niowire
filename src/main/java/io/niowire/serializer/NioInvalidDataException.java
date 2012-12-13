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
package io.niowire.serializer;

import io.niowire.NiowireException;

/**
 * This exception is thrown in the Serializers when the data they were passed to
 * serialize was invalid. It should be handled within the serializer and the
 * packet of data that caused it should be ignored.
 *
 * @author Trent Houliston
 */
public class NioInvalidDataException extends NiowireException
{
	
	//Serializeable Version UID
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor, has no message or cause.
	 */
	public NioInvalidDataException()
	{
		super();
	}

	/**
	 * Construct a new NioInvalidDataException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public NioInvalidDataException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new NioInvalidDataException with a message and a cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause of this exception
	 */
	public NioInvalidDataException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new NioInvalidDataException with a cause
	 *
	 * @param cause the cause of this exception
	 */
	public NioInvalidDataException(Throwable cause)
	{
		super(cause);
	}
}
