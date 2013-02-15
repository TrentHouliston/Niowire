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
 * A converter is responsible for converting an object of one type into an
 * object of another type. It needs to accept parameters of the types it
 * specifies and convert them to it's output type.
 *
 * If the method convertsToSuperclasses is true then if the Object that this is
 * being converted to is a superclass of our conversion then we will proceed,
 * otherwise we will be ignored.
 *
 * If convertsFromSubclasses is true then we will attempt to convert even if the
 * object we are passed is a subclass of our desired type.
 *
 * @param <T> the type we are converting from (for convenience, type erasure
 *            means we ignore this)
 * @param <F> the type we are converting from (for convenience, type erasure
 *            means we ignore this)
 *
 * @author Trent Houliston
 */
public interface Converter<F, T>
{

	/**
	 * This method is called
	 *
	 * @param from     the object that we are converting from (may be a subtype
	 *                    if
	 * @param actualTo the actual runtime type of the class we are trying to
	 *                    convert to (may be a supertype)
	 *
	 * @return the converted object of type T
	 *
	 * @throws TryNextConverterException this is thrown if the current converter
	 *                                      thinks that the next converter should
	 *                                      try to convert the object.
	 */
	public abstract T convert(F from, Class<? extends T> actualTo) throws TryNextConverterException;

	/**
	 * The class that this converter converts to
	 *
	 * @return the to
	 */
	public abstract Class<T> getTo();

	/**
	 * The class that this converter converts from
	 *
	 * @return the from
	 */
	public abstract Class<F> getFrom();

	/**
	 * If the class that this is converting to needs to be exact or if it is
	 * capable of converting to subclasses based on the runtime type.
	 *
	 * @return true if the class this is converting to must be the exact class,
	 *            false if it may be a superclass
	 */
	public abstract boolean isAdaptive();
}
