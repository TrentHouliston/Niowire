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
 * This converter converts Number objects into other numbers based on their
 * primitive class.
 *
 * @author Trent Houliston
 */
public class Number2Primitive
{

	/**
	 * Converts any number object into another number object based on their
	 * primitive type
	 *
	 * @param from the number to convert from
	 * @param to   a primitive number type to convert to
	 *
	 * @return a new number of the type specified by the primitive
	 *
	 * @throws TryNextConverterException
	 */
	public static Number convert(Number from, Class<?> to) throws TryNextConverterException
	{
		//Convert them to a primitive
		if (to == byte.class)
		{
			return from.byteValue();
		}
		else if (to == short.class)
		{
			return from.shortValue();
		}
		else if (to == int.class)
		{
			return from.intValue();
		}
		else if (to == long.class)
		{
			return from.longValue();
		}
		else if (to == float.class)
		{
			return from.floatValue();
		}
		else if (to == double.class)
		{
			return from.doubleValue();
		}
		else
		{
			throw new TryNextConverterException();
		}
	}

	/**
	 * Utility class
	 */
	private Number2Primitive()
	{
	}
}
