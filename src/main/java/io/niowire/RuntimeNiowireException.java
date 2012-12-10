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
 *
 * @author trent
 */
public class RuntimeNiowireException extends RuntimeException
{

	public RuntimeNiowireException()
	{
		super();
	}

	public RuntimeNiowireException(String message)
	{
		super(message);
	}

	public RuntimeNiowireException(String message, Exception cause)
	{
		super(message, cause);
	}

	public RuntimeNiowireException(Exception cause)
	{
		super(cause);
	}
}
