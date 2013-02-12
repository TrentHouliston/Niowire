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
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

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

	/**
	 * This method attempts to convert an object to an arbitrary type. It tries
	 * a few methods to achieve this (by no means exhaustive) and then throws an
	 * exception if it cannot.
	 *
	 * What it tries (in order) is the following
	 *
	 * Attempt to simply cast the object if it can be cast
	 *
	 * Attempt to convert it into a {@link Date} object if the target class is a
	 * date object
	 *
	 * Attempt to convert it into a {@link Calendar} if the target class is a
	 * calendar object
	 *
	 * If the source object is a number and the target type is also a number
	 * then make it the right kind of number
	 *
	 * If the to class has a static forName method which accepts a
	 * {@link String}, and our object is a String attempt to use that
	 *
	 * If the to class has a valueOf method and it accepts our object type then
	 * use that
	 *
	 * finally if the target class has a constructor which accepts our object
	 * type as the parameter, then use that
	 *
	 * @param <T>  they type of the object that will be returned
	 * @param from the object which we are converting from
	 * @param to   the class type to try to convert this object to
	 *
	 * @return a class of type T based on this object
	 */
	@SuppressWarnings(
	{
		"unchecked", "unchecked", "unchecked"
	})
	public static <T> T convert(Object from, Class<T> to)
	{
		try
		{
			//Check for nulls (can always be assigned unless it's a primitive)
			if(from == null)
			{
				return null;
			}

			//Try to do a simple cast
			if (to.isAssignableFrom(from.getClass()))
			{
				return (T) from;
			}

			//Check if it's a boolean
			if (to == boolean.class)
			{
				if (from instanceof Boolean)
				{
					return (T) from;
				}
				else if (from instanceof String)
				{
					return (T) Boolean.valueOf((String) from);
				}
			}

			//Check if we need to parse it as a date
			if (Date.class.isAssignableFrom(to))
			{
				//Use ISO8601 format
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				return (T) dateFormat.parse((String) from);
			}

			//Check if we need to parse it as a calendar
			if (Calendar.class.isAssignableFrom(to))
			{
				//Use ISO8601 format
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
				Calendar cal = Calendar.getInstance();
				cal.setTimeInMillis(dateFormat.parse((String) from).getTime());
				return (T) cal;
				//Perform a calendar conversion
			}

			//Check if we are converting it into another factory and we have a map
			if (to == NioObjectFactory.class && from instanceof Map)
			{
				Map<?, ?> map = (Map<?, ?>) from;
				if (map.containsKey("class") && map.containsKey("configuration") && map.get("configuration") instanceof Map)
				{
					return (T) new NioObjectFactory(convert(map.get("class"), Class.class), (Map<?, ?>) map.get("configuration"));
				}
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

			//Check if there is a valueOf method we can use
			try
			{
				Method valueOf = to.getMethod("valueOf", from.getClass());

				return (T) valueOf.invoke(null, from);
			}
			catch (Exception ex)
			{
				//That didn't work either!
			}

			try
			{
				//Check if there is a constructor we can use
				Constructor<T> con = to.getConstructor(from.getClass());
				return con.newInstance(from);
			}
			catch (Exception ex)
			{
				//Well that was our last guess, You're on your own now
			}
		}
		catch (Exception ex)
		{
			//WE FAIL! can't convert it
			throw new RuntimeNiowireException("Was unable to convert the parameter", ex);
		}
		//WE FAIL! can't convert it
		throw new RuntimeNiowireException("Was unable to convert the parameter");
	}

	/**
	 * Utility classes should not be constructable
	 */
	private UniversalConverter()
	{
	}
}
