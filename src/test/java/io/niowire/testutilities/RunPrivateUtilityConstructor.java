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
package io.niowire.testutilities;

import java.lang.reflect.Constructor;

/**
 * This class is designed to run the private constructor of utility classes in
 * order to more realistically display the code coverage.
 *
 * @author Trent
 */
public class RunPrivateUtilityConstructor
{

	/**
	 * Run the noargs constructor of this class (regardless of access level)
	 *
	 * @param clazz the class to run the constructor on
	 *
	 * @throws Exception
	 */
	public static void runConstructor(Class<?> clazz) throws Exception
	{
		//Get the constructor, set it to accessable and run it
		Constructor<?> constructor = clazz.getDeclaredConstructor();
		constructor.setAccessible(true);
		constructor.newInstance();
	}

	/**
	 * As this is a utility class it has it's own private constructor!
	 */
	private RunPrivateUtilityConstructor()
	{
	}
}
