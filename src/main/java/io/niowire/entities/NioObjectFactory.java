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

import io.niowire.NiowireException;
import java.util.Collections;
import java.util.Map;

/**
 * This class is used to create NioObjects using reflection from a Class Name
 * and a configuration. It will use these to construct and configure the class
 * before returning it.
 *
 * @author Trent Houliston
 */
public class NioObjectFactory<T extends NioObject>
{

	private final String className;
	private final Map<String, Object> configuration;
	private final Class<T> clazz;

	/**
	 * This constructs a new Object factory using the passed class name and the
	 * configuration object.
	 *
	 * @param className     the class name of the object
	 * @param configuration the configuration map to be used when creating the
	 *                         object
	 *
	 * @throws NiowireException if the class was not found
	 */
	public NioObjectFactory(String className, Map<String, Object> configuration) throws NioObjectCreationException
	{
		try
		{
			//Store our class name
			this.className = className;

			//Create a class object from our class name
			this.clazz = (Class<T>) Class.forName(className);

			//Check the class we just found is actually a NioObject
			if(!NioObject.class.isAssignableFrom(clazz))
			{
				throw new NioObjectCreationException(className + " is does not implement NioObject");
			}

			//Store our configuration in an unmodifyable way
			this.configuration = Collections.unmodifiableMap(configuration);
		}
		catch (Exception ex)
		{
			throw new NioObjectCreationException(ex);
		}
	}

	/**
	 * This method creates a new NioObject from the class name and configuration
	 * which were passed to this object.
	 *
	 * @return the newly created and configured NioObject
	 *
	 * @throws NioObjectCreationException if there was an exception while trying
	 *                                       to create this object.
	 */
	public T create() throws NioObjectCreationException
	{
		try
		{
			//Create a new instance
			T obj = clazz.newInstance();

			//Configure this instance
			obj.configure(configuration);
			return obj;
		}
		catch (Exception ex)
		{
			throw new NioObjectCreationException(ex);
		}
	}
}