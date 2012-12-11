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
package io.niowire.server;

import io.niowire.NiowireException;

/**
 * This exception is thrown when an existing server is attempted to be updated.
 * and it cannot change the property without restarting (e.g. changing the port)
 *
 * @author Trent Houliston
 */
public class NioPropertyUnchangableException extends NiowireException
{

	/**
	 * Default constructor, has no message or cause.
	 */
	public NioPropertyUnchangableException()
	{
		super();
	}

	/**
	 * Construct a new NioPropertyUnchangableException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public NioPropertyUnchangableException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new NioPropertyUnchangableException with a message and a
	 * cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause to attach to this exception
	 */
	public NioPropertyUnchangableException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new NioPropertyUnchangableException with a cause
	 *
	 * @param cause the cause to attach to this exception
	 */
	public NioPropertyUnchangableException(Throwable cause)
	{
		super(cause);
	}
}
