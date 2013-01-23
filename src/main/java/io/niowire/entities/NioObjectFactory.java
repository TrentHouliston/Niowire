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
 * @param <T> the type of object that this factory returns
 *
 * @author Trent Houliston
 */
public class NioObjectFactory<T>
{

	private final Class<T> clazz;
	private final Injector<T> injector;

	/**
	 * This constructs a new Object Factory using the passed className
	 *
	 * @param className the className to set
	 *
	 * @throws ClassNotFoundException if the class was not found
	 */
	@SuppressWarnings("unchecked")
	public NioObjectFactory(String className) throws ClassNotFoundException
	{
		this((Class<T>) Class.forName(className));
	}

	/**
	 * This constructs a new Object Factory using the passed class and an empty
	 * configuration
	 *
	 * @param clazz the class to create the factory for
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
	@SuppressWarnings("unchecked")
	public NioObjectFactory(String className, Map<String, ? extends Object> configuration) throws ClassNotFoundException
	{
		this((Class<T>) Class.forName(className), configuration);
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
		//Build our configuration
		injector = new Injector<T>(clazz, configuration);

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
		return create(Collections.<String, Object>emptyMap());
	}

	/**
	 * This method creates a new NioObject from the class name and configuration
	 * which were passed to this object. It also uses the passed map to provide
	 * custom injections to the created object
	 *
	 * @param injections the additional objects to inject on this creation
	 *
	 * @return the newly created and configured NioObject
	 *
	 * @throws NioObjectCreationException if there was an exception while trying
	 *                                       to create this object.
	 */
	public T create(Map<String, ? extends Object> injections) throws NioObjectCreationException
	{
		try
		{
			//Create a class object from our class name
			T obj = clazz.newInstance();

			//Inject
			injector.inject(obj, injections);

			//Return the object
			return obj;
		}
		//Explicitly catch the runtime exception, we want to catch everything
		catch (RuntimeException ex)
		{
			throw new NioObjectCreationException(ex);
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
	public boolean isInstance(Object obj)
	{
		return injector.isSame(obj);
	}
}
