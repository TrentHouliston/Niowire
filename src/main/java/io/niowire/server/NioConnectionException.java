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
 *
 * @author trent
 */
public class NioConnectionException extends NiowireException
{

	public NioConnectionException()
	{
		super();
	}

	public NioConnectionException(String message, Exception cause)
	{
		super(message, cause);
	}

	public NioConnectionException(Exception cause)
	{
		super(cause);
	}
}
