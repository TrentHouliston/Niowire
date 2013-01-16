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

import io.niowire.RuntimeNiowireException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

/**
 * This class is responsible for converting an arbritrary object into an object
 * of another type. It will make a best effort to achieve this, first by trying
 * simple casting, then number/date conversion, and then if all that fails it
 * will look for a static forName method, and finally a constructor which takes
 * the passed object as an argument.
 *
 * @author Trent Houliston
 */
public class UniversalConverter
{

	@SuppressWarnings("unchecked")
	public static <T> T convert(Object from, Class<T> to)
	{
		//Try to do a simple cast
		if (to.isAssignableFrom(from.getClass()))
		{
			return (T) from;
		}

		//Check if we need to parse it as a date
		if (Date.class.isAssignableFrom(to))
		{
			//Perform a date conversion
		}

		//Check if we need to parse it as a calendar
		if (Calendar.class.isAssignableFrom(to))
		{
			//Perform a calendar conversion
		}

		//Check if we need to convert it to a number of some description
		if (Number.class.isInstance(from))
		{
			Number n = (Number) from;

			if (to == Byte.class || to == byte.class)
			{
				return (T) (Byte) n.byteValue();
			}
			if (to == Short.class || to == short.class)
			{
				return (T) (Short) n.shortValue();
			}
			if (to == Integer.class || to == int.class)
			{
				return (T) (Integer) n.intValue();
			}
			if (to == Long.class || to == long.class)
			{
				return (T) (Long) n.longValue();
			}
			if (to == Float.class || to == float.class)
			{
				return (T) (Float) n.floatValue();
			}
			if (to == Double.class || to == double.class)
			{
				return (T) (Double) n.doubleValue();
			}
		}


		//Check if there is a forName method we can use
		try
		{
			if (from instanceof String)
			{
				Method forName = to.getMethod("forName", String.class);

				return (T) forName.invoke(null, from);
			}
		}
		catch (Exception ex)
		{
			//Well that didn't work
		}

		try
		{
			//Check if there is a constructor we can use
			Constructor<T> con = to.getConstructor(from.getClass());
			return con.newInstance(from);
		}
		catch (Exception ex)
		{
		}

		//WE FAIL! can't convert it
		throw new RuntimeNiowireException("Was unable to convert the parameter");
	}
}
