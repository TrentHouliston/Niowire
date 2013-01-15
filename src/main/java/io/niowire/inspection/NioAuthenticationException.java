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
package io.niowire.inspection;

import io.niowire.NiowireException;

/**
 * This exception is thrown when a packet fails authenticating. When this is
 * thrown then the connection to the client will be closed and the NioConnection
 * object and all services will be shut down.
 *
 * @author Trent Houliston
 */
public class NioAuthenticationException extends NiowireException
{
	//Serial Version UID
	private static final long serialVersionUID = 1L;

	/**
	 * Default constructor, has no message or cause.
	 */
	public NioAuthenticationException()
	{
		super();
	}

	/**
	 * Construct a new NioAuthenticationException with the passed message
	 *
	 * @param message the message to attach to this exception
	 */
	public NioAuthenticationException(String message)
	{
		super(message);
	}

	/**
	 * Construct a new NioAuthenticationException with a message and a cause.
	 *
	 * @param message the message to attach to this exception
	 * @param cause   the cause of this exception
	 */
	public NioAuthenticationException(String message, Throwable cause)
	{
		super(message, cause);
	}

	/**
	 * Constructs a new NioAuthenticationException with a cause
	 *
	 * @param cause the cause of this exception
	 */
	public NioAuthenticationException(Throwable cause)
	{
		super(cause);
	}
}

