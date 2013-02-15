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
 * This class converts Number classes to the basic Java Number classes
 *
 * @author Trent Houliston
 */
public class Number2Number implements Converter<Number, Number>
{

	/**
	 * Converts any number object into one of the java basic number types
	 *
	 * @param from     the number to convert from
	 * @param actualTo the actual number to convert to
	 *
	 * @return the converted number
	 *
	 * @throws TryNextConverterException if we could not convert it (not one of
	 *                                      the basic number classes)
	 */
	@Override
	public Number convert(Number from, Class<? extends Number> actualTo) throws TryNextConverterException
	{
		if (Byte.class.isAssignableFrom(actualTo))
		{
			return from.byteValue();
		}
		else if (Short.class.isAssignableFrom(actualTo))
		{
			return from.shortValue();
		}
		else if (Integer.class.isAssignableFrom(actualTo))
		{
			return from.intValue();
		}
		else if (Long.class.isAssignableFrom(actualTo))
		{
			return from.longValue();
		}
		else if (Float.class.isAssignableFrom(actualTo))
		{
			return from.floatValue();
		}
		else if (Double.class.isAssignableFrom(actualTo))
		{
			return from.doubleValue();
		}
		throw new TryNextConverterException();
	}

	/**
	 * We convert to Number classes (subclasses)
	 *
	 * @return Number.class
	 */
	@Override
	public Class<Number> getTo()
	{
		return Number.class;
	}

	/**
	 * We convert from Number classes
	 *
	 * @return Number.class
	 */
	@Override
	public Class<Number> getFrom()
	{
		return Number.class;
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