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

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

/**
 * This class is responsible for converting an arbritrary object into an object
 * of another type. It will make a best effort to achieve this, first by trying
 * simple casting, then number/date conversion, and then if all that fails it
 * will look for a static forName method, and finally a constructor which takes
 * the passed object as an argument.
 *
 * @author Trent Houliston
 */
public class UniversalConverter
{

	private final LinkedList<Converter<?, ?>> converters = new LinkedList<Converter<?, ?>>();

	/**
	 * Constructs a new Universal Converter
	 */
	public UniversalConverter()
	{
		//Add the boxed converters
		addConverter(new String2Boolean());
		addConverter(new String2Character());
		addConverter(new String2Number());
		addConverter(new Number2Number());

		//Add the Niowire specific converters
		addConverter(new Map2NioObjectFactory());

		//Add the fallback converters
		addConverter(new String2Object());
		addConverter(new Object2String());
		addConverter(new Object2Object());
	}

	/**
	 * Gets the global default UniversalConverter
	 *
	 * @return the default UniversalConverter
	 */
	public static UniversalConverter getInstance()
	{
		return Singleton.INSTANCE;
	}

	/**
	 * A static convenience method to get the default converter to convert the
	 * object.
	 *
	 * @param <T>  they type of the object that will be returned
	 * @param from the object which we are converting from
	 * @param to   the class type to try to convert this object to
	 *
	 * @return a class of type T based on this object
	 *
	 * @throws UnconvertableObjectException if the object could not be converted
	 */
	public static <T> T doConvert(Object from, Class<T> to) throws UnconvertableObjectException
	{
		return Singleton.INSTANCE.convert(from, to);
	}

	/**
	 * This method attempts to convert an object to an arbitrary type. It tries
	 * a few methods to achieve this (by no means exhaustive) and then throws an
	 * exception if it cannot.
	 *
	 * It first tries simply to do a cast, and will then go through it's list of
	 * converters to see if one matches the purpose
	 *
	 * @param <T>  they type of the object that will be returned
	 * @param from the object which we are converting from
	 * @param to   the class type to try to convert this object to
	 *
	 * @return a class of type T based on this object
	 *
	 * @throws UnconvertableObjectException if the object could not be converted
	 */
	public <T> T convert(Object from, Class<T> to) throws UnconvertableObjectException
	{
		//Check for nulls (can always be assigned unless it's a primitive)
		if (from == null)
		{
			return null;
		}
		//Try to do a simple cast
		else if (to.isInstance(from))
		{
			//We did just do a check with the isAssignableFrom
			@SuppressWarnings("unchecked")
			T result = (T) from;
			return result;
		}
		else if (from instanceof String && to.isPrimitive())
		{
			String in = (String) from;
			//Its checked, don't worry
			@SuppressWarnings("unchecked")
			T result = (T) String2Primitive.convert(in, to);
			return result;
		}
		else if (from instanceof Number && to.isPrimitive())
		{
			try
			{
				Number in = (Number) from;
				//It's checked don't worry
				@SuppressWarnings("unchecked")
				T result = (T) Number2Primitive.convert(in, to);
				return result;
			}
			catch (TryNextConverterException ex)
			{
			}
		}

		LinkedList<Converter<?, ?>> validConverters = new LinkedList<Converter<?, ?>>();

		for (Converter<?, ?> c : converters)
		{
			//Adaptive converters check if what they are converting to is a subclass rather then superclass
			boolean convertsTo = c.isAdaptive() ? c.getTo().isAssignableFrom(to) : to.isAssignableFrom(c.getTo());
			boolean convertsFrom = c.getFrom().isInstance(from);
			if (convertsTo && convertsFrom)
			{
				validConverters.add(c);
			}
		}

		//We now have our list of vaild converters, sort it in order of best fit
		Collections.sort(validConverters, new BestFit(from.getClass(), to));

		for (Converter c : validConverters)
		{
			try
			{
				return (T) c.convert(from, to);
			}
			catch (TryNextConverterException ignore)
			{
				//Ignore and try the next converter
			}
		}

		//Try casting it anyway... You never know it might work (e.g from boolean 2 Boolean)
		try
		{
			return (T) from;
		}
		catch (Exception ex)
		{
			//Well that was our last guess, You're on your own now
			throw new UnconvertableObjectException("The last ditch effort to convert this object failed");
		}
	}

	/**
	 * This method adds a new converter to our list of available converters
	 *
	 * @param converter the converter to add to the list.
	 */
	public final void addConverter(Converter<?, ?> converter)
	{
		converters.add(converter);
	}

	/**
	 * This comparator is used for comparing the converters to find the best
	 * suited one from our options.
	 */
	private static class BestFit implements Comparator<Converter<?, ?>>, Serializable
	{

		private static final long serialVersionUID = 1L;
		//Our to and from classes
		private final Class<?> from;
		private final Class<?> to;

		/**
		 * Create a new BestFit comparator which finds the closest fit between
		 * the converters for converting from the from class to the to class. It
		 * picks the converter which has the best to closeness and then the best
		 * from closeness.
		 *
		 * @param from the class we are converting from
		 * @param to   the class we are converting to
		 */
		private BestFit(Class<?> from, Class<?> to)
		{
			this.from = from;
			this.to = to;
		}

		/**
		 * This method compares the two converters to find the converter with
		 * the closest fit to the conversion we are doing.
		 *
		 * @param c1 the first converter to compare
		 * @param c2 the second converter to compare
		 *
		 * @return an index stating if the first comparator is better worse or
		 *               the same as the second comparator
		 */
		@Override
		public int compare(Converter<?, ?> c1, Converter<?, ?> c2)
		{
			/*
			 * Primary sort on distance, if it's adaptive then we reverse the
			 * way we look (towards subclass distance rather then superclasses)
			 */
			int c1to = c1.isAdaptive() ? getSuperclassDistance(to, c1.getTo()) : getSuperclassDistance(c1.getTo(), to);
			int c2to = getSuperclassDistance(c2.getTo(), to);

			//Secondary sort distance
			int c1from = getSuperclassDistance(from, c1.getFrom());
			int c2from = getSuperclassDistance(from, c1.getFrom());
			Integer.compare(c1to, c2to);

			//Sort by primary then secondary
			return Integer.compare(c1to, c2to) == 0 ? Integer.compare(c1from, c2from) : Integer.compare(c1to, c2to);
		}

		/**
		 * This method gets the distance (in terms of superclasses) between the
		 * passed subclass and the passed superclass
		 *
		 * @param subclass   the subclass to start at
		 * @param superclass the superclass to aim for
		 *
		 * @return the number of classes in between this class and the
		 *               superclass (0 if they are the same class)
		 *
		 * @throws NullPointerException if the passed superclass is not in the
		 *                                    parents of the subclass
		 */
		public static int getSuperclassDistance(Class<?> subclass, Class<?> superclass) throws NullPointerException
		{
			//Start at 0
			int distance = 0;

			//Loop through each time moving up the chain
			Class<?> currentClass = subclass;
			while (currentClass != null && currentClass != superclass)
			{
				distance++;
				currentClass = currentClass.getSuperclass();
			}

			//Return our distance
			return distance;
		}
	}

	/**
	 * The singleton instance for the default UniversalConverter
	 */
	private static final class Singleton
	{

		private static final UniversalConverter INSTANCE = new UniversalConverter();
	}
}
