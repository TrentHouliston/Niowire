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

/**
 * This exception is the common super class for all of the Runtime exceptions
 * thrown by Niowire. These kind of exceptions are to be thought of as
 * exceptions that you cannot reasonably be expected to correct, or recover
 * from.
 *
 * @author Trent Houliston
 */
public class RuntimeNiowireException extends RuntimeException
{

	/**
	 * Default constructor, has no message or cause.
	 */
	public RuntimeNiowireException()
	{
		super();
	}

	/**
	 * Construct a new RuntimeNiowireException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public RuntimeNiowireException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new RuntimeNiowireException with a message and a cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause to attach to this exception
	 */
	public RuntimeNiowireException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new RuntimeNiowireException with a cause
	 *
	 * @param cause the cause to attach to this exception
	 */
	public RuntimeNiowireException(Throwable cause)
	{
		super(cause);
	}
}
