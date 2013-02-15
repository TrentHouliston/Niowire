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

import io.niowire.entities.convert.UniversalConverter;
import io.niowire.RuntimeNiowireException;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import javax.inject.Inject;
import javax.inject.Named;

/**
 * This object is responsible for injecting data based on annotations into the
 * passed object. It uses reflection to inject data into the fields with the
 * {@link Inject} annotation. It then runs all methods which are annotated with
 * {@link Initialize}
 *
 * @param <T> the type of object that this injector injects into
 */
public class Injector<T>
{

	private final Map<Field, ? super Object> fields;
	private final List<Method> methods;
	private final Class<?> clazz;

	/**
	 * This method builds a map of the fields to the configuration parameters
	 * which should go into them based on the {@link Inject} annotations on the
	 * class (and superclass)
	 *
	 * @param clazz         the class to build the Fields map from
	 * @param configuration the configuration to build the values from
	 */
	/**
	 * This method builds a map of the fields to the configuration parameters
	 * which should go into them based on the {@link Inject} annotations on the
	 * class (and superclass)
	 *
	 * @param clazz         the class to build the Fields map from
	 * @param configuration the configuration to build the values from
	 */
	public Injector(Class<T> clazz, Map<String, ? extends Object> configuration)
	{
		this.clazz = clazz;

		//Build our fields map and our list of methods to run
		this.fields = buildFields(clazz, configuration);
		this.methods = buildMethods(clazz);
	}

	/**
	 * This method injects all the variables from the Injector into the object.
	 * It runs all methods which are marked with an {@link Initialize}
	 * annotation
	 *
	 * @param o the object to inject into
	 */
	public void inject(T o)
	{
		//Inject with an empty additionals map
		inject(o, Collections.<String, Object>emptyMap());
	}

	/**
	 * This method injects all the variables from the Injector into the object,
	 * and then injects from the injections object. It runs all methods which
	 * are marked with an {@link Initialize} annotation
	 *
	 * @param o          the object to inject into
	 * @param injections additional injections to perform
	 */
	public void inject(T o, Map<String, ? extends Object> injections)
	{
		//Create a combined map of the base fields overriden with the extra injections
		Map<Field, ? super Object> fieldsToInject = new HashMap<Field, Object>(fields);
		fieldsToInject.putAll(buildFields(clazz, injections));

		//Check if we should even bother
		if (!fieldsToInject.isEmpty())
		{
			try
			{
				//Loop through each of our fields
				for (Entry<Field, ? extends Object> entry : fieldsToInject.entrySet())
				{
					//Set our value
					entry.getKey().set(o, entry.getValue());
				}
			} //If an exception occurs then wrap and throw it
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
			//If it was an invocation exception throw its cause (probably more useful)
			catch (InvocationTargetException ex)
			{
				throw new RuntimeNiowireException(ex.getCause());
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
	 * Checks if the passed object has all of it's injectable fields equal to
	 * this injector
	 *
	 * @param obj the object to check
	 *
	 * @return if the objects fields are what the injector would inject
	 */
	public boolean isSame(Object obj)
	{
		//Test for null
		if (obj == null)
		{
			return false;
		}
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

	/**
	 * This method builds a map of fields to the objects that are to be injected
	 * into them based on the passed class and configuration
	 *
	 * @param clazz         the class to inject into
	 * @param configuration the configuration to build the injection from
	 *
	 * @return a map of Fields to the objects to be injected into them
	 */
	private static Map<Field, ? super Object> buildFields(Class<?> clazz, Map<String, ? extends Object> configuration)
	{
		//First get all our fields (if there are fields to get)
		if (configuration != null && !configuration.isEmpty())
		{
			LinkedList<Field> allFields = new LinkedList<Field>();
			Map<Field, Object> fields = new HashMap<Field, Object>(10);
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
						Object value = UniversalConverter.doConvert(configuration.get(source), f.getType());
						//Add the injection to our map
						fields.put(f, value);
					}
				}
			}
			return fields;
		}
		else
		{
			//Otherwise we will use an empty map as our config
			return Collections.<Field, Object>emptyMap();
		}
	}

	/**
	 * This method builds the list of methods in the passed class that are
	 * decorated with the {@link Initialize} annotation
	 *
	 * @param clazz the class to get the methods from
	 *
	 * @return a list of methods with the {@link Initialize} annotation
	 */
	private static List<Method> buildMethods(Class<?> clazz)
	{
		LinkedList<Method> methods = new LinkedList<Method>();
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

		//Return the methods
		return methods;
	}
}
