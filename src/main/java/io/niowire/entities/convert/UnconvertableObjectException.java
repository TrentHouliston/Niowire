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

import io.niowire.RuntimeNiowireException;

/**
 * This exception is thrown by a converter when the object that is passed cannot
 * be converted and we should not try any further converters.
 *
 * @author Trent Houliston
 */
public class UnconvertableObjectException extends RuntimeNiowireException
{

	//Serial Version UID
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor, has no message or cause.
	 */
	public UnconvertableObjectException()
	{
		super();
	}

	/**
	 * Construct a new UnconvertableObjectException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public UnconvertableObjectException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new UnconvertableObjectException with a message and a cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause to attach to this exception
	 */
	public UnconvertableObjectException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new UnconvertableObjectException with a cause
	 *
	 * @param cause the cause to attach to this exception
	 */
	public UnconvertableObjectException(Throwable cause)
	{
		super(cause);
	}
}
