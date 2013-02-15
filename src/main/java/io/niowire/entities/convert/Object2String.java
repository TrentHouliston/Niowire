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
 * This converter converts any Object into a String by calling it's toString
 * method
 *
 * @author Trent Houliston
 */
public class Object2String implements Converter<Object, String>
{

	/**
	 * Converts any object to a String using the toString method
	 *
	 * @param from     the object to convert
	 * @param actualTo String.class
	 *
	 * @return the toString method from the object
	 */
	@Override
	public String convert(Object from, Class<? extends String> actualTo)
	{
		return from.toString();
	}

	/**
	 * We convert to String.class
	 *
	 * @return String.class
	 */
	@Override
	public Class<String> getTo()
	{
		return String.class;
	}

	/**
	 * We convert from Object.class
	 *
	 * @return Object.class
	 */
	@Override
	public Class<Object> getFrom()
	{
		return Object.class;
	}

	/**
	 * We are not adaptive (we only convert to Strings)
	 *
	 * @return false
	 */
	@Override
	public boolean isAdaptive()
	{
		return false;
	}
}