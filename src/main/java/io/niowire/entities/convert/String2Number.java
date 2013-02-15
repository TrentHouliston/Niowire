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
 * This converter converts a String into a number of the designated class. It is
 * an adaptive converter and will attempt to convert any class that is a
 * subclass of Number.
 *
 * @author Trent Houliston
 */
public class String2Number implements Converter<String, Number>
{

	/**
	 * Attempt to convert a String into a number class
	 *
	 * @param from     the class to convert from
	 * @param actualTo the class to convert to (a subclass of Number)
	 *
	 * @return a boxed number representing the string that was passed
	 *
	 * @throws TryNextConverterException if we could not parse the number from
	 *                                      the string
	 */
	@Override
	public Number convert(String from, Class<? extends Number> actualTo) throws TryNextConverterException
	{
		if (Byte.class.isAssignableFrom(actualTo))
		{
			return Byte.parseByte(from);
		}
		else if (Short.class.isAssignableFrom(actualTo))
		{
			return Short.parseShort(from);
		}
		else if (Integer.class.isAssignableFrom(actualTo))
		{
			return Integer.parseInt(from);
		}
		else if (Long.class.isAssignableFrom(actualTo))
		{
			return Long.parseLong(from);
		}
		else if (Float.class.isAssignableFrom(actualTo))
		{
			return Float.parseFloat(from);
		}
		else if (Double.class.isAssignableFrom(actualTo))
		{
			return Double.parseDouble(from);
		}
		else
		{
			throw new TryNextConverterException();
		}
	}

	/**
	 * We convert from any subclass of Number.class (we are adaptive)
	 *
	 * @return Number.class
	 */
	@Override
	public Class<Number> getTo()
	{
		return Number.class;
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
	 * We are an adaptive converter
	 *
	 * @return true
	 */
	@Override
	public boolean isAdaptive()
	{
		return true;
	}
}