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

/**
 * This interface defines a factory which will return a new instance of the
 * object that this factory creates after it has been configured.
 *
 * @param <T> the type of NioObject which this class returns.
 *
 * @author Trent Houliston
 */
public interface NioObjectFactory<T extends NioObject>
{

	/**
	 * This method creates a new NioObject from the class name and configuration
	 * in this object
	 *
	 * @return the newly created and configured NioObject
	 *
	 * @throws NioObjectCreationException if there was an exception while trying
	 *                                       to create this object.
	 */
	public T create() throws NioObjectCreationException;
}
