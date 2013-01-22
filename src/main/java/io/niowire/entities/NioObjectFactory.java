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

import io.niowire.RuntimeNiowireException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;

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
		try
		{
			//Create a class object from our class name
			T obj = clazz.newInstance();

			//Inject
			injector.inject(obj);

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

	/**
	 * This is a configuration object which is built up based on a passed config
	 * and a class. It gathers all of the fields and the values to set them to,
	 * as well as the init methods which need to be run.
	 *
	 * @param <T> the type of object that this injector injects into
	 */
	public static class Injector<T>
	{

		private final Map<Field, ? super Object> fields;
		private final List<Method> methods;
		private final Class<?> clazz;

		/**
		 * This method builds a map of the fields to the configuration
		 * parameters which should go into them based on the {@link Inject} annotations on the
		 * class (and superclass)
		 *
		 * @param clazz         the class to build the Fields map from
		 * @param configuration the configuration to build the values from
		 */
		public Injector(Class<T> clazz, Map<String, ? extends Object> configuration)
		{
			this.clazz = clazz;

			//First get all our fields (if there are fields to get)
			if (configuration != null && !configuration.isEmpty())
			{
				LinkedList<Field> allFields = new LinkedList<Field>();
				fields = new HashMap<Field, Object>(10);

				//Get all of our fields for the chain
				Class<?> c = clazz;
				while (c != null)
				{
					allFields.addAll(Arrays.asList(c.getDeclaredFields()));
					c = c.getSuperclass();
				}

				//Loop through the fields matching up the configuration with the result
				for (Field f : allFields)
				{
					//Check for the @Inject and @Named annotations
					boolean inject = f.getAnnotation(Inject.class) != null;
					Named name = f.getAnnotation(Named.class);

					//If we are injecting
					if (inject)
					{
						//Set our field to be accessable
						f.setAccessible(true);

						//Get the value we are injecting from
						String source = (name == null || name.value().isEmpty()) ? f.getName() : name.value();

						//Check we have that config option
						if (configuration.containsKey(source))
						{
							//Convert our value
							Object value = UniversalConverter.convert(configuration.get(source), f.getType());

							//Add the injection to our map
							fields.put(f, value);
						}
					}
				}
			}
			else
			{
				//Otherwise we will use an empty map as our config
				fields = Collections.<Field, Object>emptyMap();
			}

			methods = new LinkedList<Method>();

			//Get all of our fields for the chain
			Class<?> c = clazz;
			while (c != null)
			{
				methods.addAll(Arrays.asList(c.getDeclaredMethods()));
				c = c.getSuperclass();
			}

			//Loop through all the methods removing the ones with no annotation
			for (Iterator<Method> it = methods.iterator(); it.hasNext();)
			{
				Method next = it.next();

				if (next.getAnnotation(Initialize.class) != null)
				{
					if (next.getParameterTypes().length > 0)
					{
						throw new UnsupportedOperationException("Initialize methods must take no parameters");
					}
					else if (next.getAnnotation(Initialize.class) != null)
					{
						next.setAccessible(true);
					}
				}
				else
				{
					it.remove();
				}
			}
		}

		/**
		 * This method injects all the variables from the Injector into the
		 * object and runs all methods which are marked with an
		 * {@link Initialize} annotation
		 *
		 * @param o the object to inject into
		 */
		public void inject(T o)
		{
			//Check if we should even bother
			if (!this.fields.isEmpty())
			{
				try
				{
					//Loop through each of our fields
					for (Entry<Field, ? extends Object> entry : this.fields.entrySet())
					{
						//Set our value
						entry.getKey().set(o, entry.getValue());
					}
				}
				//If an exception occurs then wrap and throw it
				catch (RuntimeException ex)
				{
					throw new RuntimeNiowireException(ex);
				}
				catch (Exception ex)
				{
					throw new RuntimeNiowireException(ex);
				}
			}
			//Loop through all the methods
			for (Method m : this.methods)
			{
				try
				{
					m.invoke(o);
					//If an exception occurs then wrap and throw it
				}
				catch (RuntimeException ex)
				{
					throw new RuntimeNiowireException(ex);
				}
				catch (Exception ex)
				{
					throw new RuntimeNiowireException(ex);
				}
			}
		}

		/**
		 * Checks if the passed object has all of it's injectable fields equal
		 * to this injector
		 *
		 * @param obj the object to check
		 *
		 * @return if the objects fields are what the injector would inject
		 */
		public boolean isSame(Object obj)
		{
			try
			{
				//Test the object is the correct class
				if (obj.getClass() == clazz)
				{
					for (Entry<Field, ? extends Object> entry : fields.entrySet())
					{
						if (!entry.getKey().get(obj).equals(entry.getValue()))
						{
							return false;
						}
					}
					return true;
				}
				else
				{
					return false;
				}
			}
			catch (IllegalAccessException ex)
			{
				return false;
			}
		}
	}
}
