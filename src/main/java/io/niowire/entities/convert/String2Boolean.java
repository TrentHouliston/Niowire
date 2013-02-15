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
 * This class converts a string of the name True or False (case insensitive)
 * into a boolean
 *
 * @author Trent Houliston
 */
public class String2Boolean implements Converter<String, Boolean>
{

	/**
	 * Converts a String into a Boolean object
	 *
	 * @param from     the String to convert from
	 * @param actualTo Boolean.class
	 *
	 * @return the converted boolean
	 *
	 * @throws TryNextConverterException if we could not convert this class
	 */
	@Override
	public Boolean convert(String from, Class<? extends Boolean> actualTo) throws TryNextConverterException
	{
		return Boolean.parseBoolean(from);
	}

	/**
	 * We convert to Boolean.class
	 *
	 * @return Boolean.class
	 */
	@Override
	public Class<Boolean> getTo()
	{
		return Boolean.class;
	}

	/**
	 * We convert from String.class
	 *
	 * @return String.class
	 */
	@Override
	public Class<String> getFrom()
	{
		return String.class;
	}

	/**
	 * We are not adaptive (only convert to Boolean.class)
	 *
	 * @return false
	 */
	@Override
	public boolean isAdaptive()
	{
		return false;
	}
}