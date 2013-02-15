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

/**
 * This class converts any String into a primitive object of the passed type. It
 * is a special converter which is checked before any other converter is
 * checked.
 *
 * @author Trent Houliston
 */
public class String2Primitive
{

	/**
	 * This method converts the passed String into a primitive object of the
	 * type provided in to
	 *
	 * @param from the object to convert
	 * @param to   the class to convert the object into
	 *
	 * @return the converted primitive
	 */
	public static Object convert(String from, Class<?> to)
	{
		if (to == boolean.class)
		{
			return Boolean.parseBoolean(from);
		}
		else if (to == char.class)
		{
			if (!from.isEmpty())
			{
				return from.charAt(0);
			}
			else
			{
				//Return the null char for an empty string
				return '\u0000';
			}
		}
		else if (to == byte.class)
		{
			return Byte.parseByte(from);
		}
		else if (to == short.class)
		{
			return Short.parseShort(from);
		}
		else if (to == int.class)
		{
			return Integer.parseInt(from);
		}
		else if (to == long.class)
		{
			return Long.parseLong(from);
		}
		else if (to == float.class)
		{
			return Float.parseFloat(from);
		}
		//This will always be a double (there are no other primitives)
		else
		{
			return Double.parseDouble(from);
		}
	}

	/**
	 * Utility class has no constructor
	 */
	private String2Primitive()
	{
	}
}
