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

import java.util.Collections;
import java.util.Map;

/**
 * This class is used to create NioObjects using reflection from a Class Name
 * and a configuration. It will use these to construct and configure the class
 * before returning it.
 *
 * @param <T> the type of NioObject which this class returns.
 *
 * @author Trent Houliston
 */
public class NioObjectFactory<T extends NioObject>
{

	private final Class<?> clazz;
	private final Map<String, ? extends Object> configuration;

	/**
	 * This constructs a new Object Factory using the passed className
	 *
	 * @param className the className to set
	 *
	 * @throws ClassNotFoundException if the class was not found
	 */
	public NioObjectFactory(String className) throws ClassNotFoundException
	{
		this(className, Collections.<String, Object>emptyMap());
	}

	/**
	 * This constructs a new Object Factory using the passed class and an empty
	 * configuration
	 *
	 * @param clazz the class to b ea factory for
	 */
	public NioObjectFactory(Class<T> clazz)
	{
		this(clazz, Collections.<String, Object>emptyMap());
	}

	/**
	 * This constructs a new Object factory using the passed class name and the
	 * configuration object.
	 *
	 * @param className     the class name of the object
	 * @param configuration the configuration map to be used when creating the
	 *                         object
	 *
	 * @throws ClassNotFoundException
	 */
	public NioObjectFactory(String className, Map<String, ? extends Object> configuration) throws ClassNotFoundException
	{
		//Store our configuration in an unmodifyable way
		this.configuration = Collections.unmodifiableMap(configuration);

		//Get the class object
		clazz = Class.forName(className);
	}

	/**
	 * This constructs a new object factory using the passed class and the
	 * configuration object
	 *
	 * @param clazz         the class of the object to create
	 * @param configuration the configuration of the object to create
	 */
	public NioObjectFactory(Class<T> clazz, Map<String, ? extends Object> configuration)
	{
		//Store our configuration in an unmodifyable way
		this.configuration = Collections.unmodifiableMap(configuration);

		//Store our class
		this.clazz = clazz;
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
			//Create a class object from our class name
			Object obj = clazz.newInstance();

			if (obj instanceof NioObject)
			{
				//We did check it's type
				@SuppressWarnings("unchecked")
				T nioObj = (T) obj;

				//Configure and return
				nioObj.configure(configuration);
				return nioObj;
			}
			else
			{
				throw new NioObjectCreationException(clazz.getName() + " is does not implement NioObject");
			}

		}
		//Explicitly catch the runtime exception, we want to catch everything
		catch (RuntimeException ex)
		{
			throw new NioObjectCreationException(ex);
		}
		catch (NioObjectCreationException ex)
		{
			throw ex;
		}
		catch (Exception ex)
		{
			throw new NioObjectCreationException(ex);
		}
	}

	/**
	 * Checks if the passed object is an instance which would be created by this
	 * factory (both the type and the configuration)
	 *
	 * @param obj the object to be tested
	 *
	 * @return if the object is the same (class and configuration) as this
	 *            factory creates
	 */
	public boolean isInstance(NioObject obj)
	{
		return obj != null && clazz.equals(obj.getClass()) && this.configuration.equals(obj.getConfiguration());
	}
}
