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
 * This converter converts a String into a Character object (or null on empty
 * String)
 *
 * @author Trent Houliston
 */
public class String2Character implements Converter<String, Character>
{

	/**
	 * Converts to a Character object (or null for an empty String) It will do
	 * this by returning the first character of the String
	 *
	 * @param from     the String to convert from
	 * @param actualTo Character.class
	 *
	 * @return the first character of the String or null if there isn't one
	 *
	 * @throws TryNextConverterException if we cannot convert this
	 */
	@Override
	public Character convert(String from, Class<? extends Character> actualTo) throws TryNextConverterException
	{
		if (!from.isEmpty())
		{
			return from.charAt(0);
		}
		else
		{
			return null;
		}
	}

	/**
	 * We convert to Characters
	 *
	 * @return Character.class
	 */
	@Override
	public Class<Character> getTo()
	{
		return Character.class;
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
	 * We are not adaptive (we only convert to characters)
	 *
	 * @return false
	 */
	@Override
	public boolean isAdaptive()
	{
		return false;
	}
}