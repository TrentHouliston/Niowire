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

import java.lang.reflect.Method;

/**
 * This method attempts to convert from a String to an unknown object type. It
 * attempts to do this using the static forName method which may exist on a
 * class. (This allows converting of things like {@link Class} and
 * {@link Charset}. This is an adaptive converter which means that instead of
 * converting to a single type. It will convert to a number of objects which are
 * subclasses of what it converts to.
 *
 * @author Trent Houliston
 */
public class String2Object implements Converter<String, Object>
{

	/**
	 * Convert the passed String into an object if the class we are converting
	 * to has a forName method to run (e.g. {@link Charset#forName(String)}).
	 *
	 * @param from     the String we are converting from
	 * @param actualTo the actual runtime class we are converting to.
	 *
	 * @return a converted object created by using the forName method
	 *
	 * @throws TryNextConverterException
	 */
	@Override
	public Object convert(String from, Class<? extends Object> actualTo) throws TryNextConverterException
	{
		//Try to get our forName method and run it
		try
		{
			Method forName = actualTo.getMethod("forName", String.class);
			if (actualTo.isAssignableFrom(forName.getReturnType()))
			{
				return forName.invoke(null, from);
			}
		}
		catch (Exception ex)
		{
		}
		throw new TryNextConverterException();
	}

	/**
	 * We convert to any subclass of Object.class (anything) since we are an
	 * adaptive converter.
	 *
	 * @return Object.class
	 */
	@Override
	public Class<Object> getTo()
	{
		return Object.class;
	}

	/**
	 * We convert from Strings
	 *
	 * @return String.class
	 */
	@Override
	public Class<String> getFrom()
	{
		return String.class;
	}

	/**
	 * We are an adaptive converter (convert to subclasses of our to class)
	 *
	 * @return true
	 */
	@Override
	public boolean isAdaptive()
	{
		return true;
	}
}