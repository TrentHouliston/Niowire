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

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * This converter is a last ditch effort to try and convert. It tries to find a
 * valueOf static method which takes this object as it's parameter, or a
 * constructor which takes this as it's parameter.
 *
 * @author Trent Houliston
 */
public class Object2Object implements Converter<Object, Object>
{

	/**
	 * Try everything else we can think of to convert one object into another
	 *
	 * @param from     the object we are converting from
	 * @param actualTo the class we are trying to convert to
	 *
	 * @return an Object of type actualTo
	 *
	 * @throws TryNextConverterException
	 */
	@Override
	public Object convert(Object from, Class<? extends Object> actualTo) throws TryNextConverterException
	{
		//Check if there is a valueOf method we can use
		try
		{
			Method m;
			//AUTOBOXING IS THE DEVIL FOR REFLECTION
			if (isBoxed(from))
			{
				//We have to check for signatures with both boxed and unboxed variables
				Class<?> boxed = from.getClass();
				Class<?> unboxed = getUnboxedClass(boxed);
				try
				{
					m = actualTo.getMethod("valueOf", unboxed);
				}
				catch (NoSuchMethodException ex)
				{
					m = actualTo.getMethod("valueOf", boxed);
				}
				//Try the primitive
				//Try the box
			}
			else
			{
				m = actualTo.getMethod("valueOf", from.getClass());
			}
			//Find a valueOf method
			if (Modifier.isPublic(m.getModifiers()) && actualTo.isAssignableFrom(m.getReturnType()))
			{
				return m.invoke(null, from);
			}
		}
		catch (Exception ex)
		{
		}

		try
		{
			//Check if there is a constructor we can use
			Constructor<?> con = actualTo.getConstructor(from.getClass());
			return con.newInstance(from);
		}
		catch (Exception ex)
		{
			throw new TryNextConverterException();
		}

	}

	/**
	 * We convert to any class (all subclasses of Object)
	 *
	 * @return Object.class
	 */
	@Override
	public Class<Object> getTo()
	{
		return Object.class;
	}

	/**
	 * We convert from any Object)
	 *
	 * @return Object.class
	 */
	@Override
	public Class<Object> getFrom()
	{
		return Object.class;
	}

	/**
	 * We are adaptive
	 *
	 * @return true
	 */
	@Override
	public boolean isAdaptive()
	{
		return true;
	}

	/**
	 * Used for detecting if a parameter is boxed class
	 *
	 * @param from the object we are converting from
	 *
	 * @return if the parameter was one of the boxed primitive types
	 */
	private static boolean isBoxed(Object from)
	{
		return from.getClass() == Byte.class
			   || from.getClass() == Short.class
			   || from.getClass() == Integer.class
			   || from.getClass() == Long.class
			   || from.getClass() == Float.class
			   || from.getClass() == Double.class
			   || from.getClass() == Character.class
			   || from.getClass() == Boolean.class;
	}

	/**
	 * Gets the primitive class for this boxed class
	 *
	 * @param clazz the class to get the primitive for
	 *
	 * @return the primitive class for this object (or null if it's not a
	 *            primitive)
	 */
	private static Class<?> getUnboxedClass(Class<?> clazz)
	{
		return clazz == Byte.class ? byte.class
			   : clazz == Short.class ? short.class
				 : clazz == Integer.class ? int.class
				   : clazz == Long.class ? long.class
					 : clazz == Float.class ? float.class
					   : clazz == Double.class ? double.class
						 : clazz == Character.class ? char.class
						   : clazz == Boolean.class ? boolean.class
							 : null;
	}
}