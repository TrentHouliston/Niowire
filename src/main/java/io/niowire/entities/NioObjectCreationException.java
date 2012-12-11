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

import io.niowire.NiowireException;

/**
 * This exception is thrown when while trying to create a new NioObject there
 * was an exception that occurred. This is the wrapper for all the exceptions
 * which are thrown during this process.
 *
 * @author Trent Houliston
 */
public class NioObjectCreationException extends NiowireException
{

	/**
	 * Default constructor, has no message or cause.
	 */
	public NioObjectCreationException()
	{
		super();
	}

	/**
	 * Construct a new NioObjectCreationException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public NioObjectCreationException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new NioObjectCreationException with a message and a cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause of this exception
	 */
	public NioObjectCreationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new NioObjectCreationException with a cause
	 *
	 * @param cause the cause of this exception
	 */
	public NioObjectCreationException(Throwable cause)
	{
		super(cause);
	}
}
