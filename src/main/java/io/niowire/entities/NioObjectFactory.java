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

import java.util.Map;

/**
 *
 * @author trent
 */
public class NioObjectFactory<T extends NioObject>
{

	private final String className;
	private final Map<String, Object> configuration;

	public NioObjectFactory(String className, Map<String, Object> configuration)
	{
		this.className = className;
		this.configuration = configuration;
	}

	public T create() throws NioEntityCreationException
	{
		try
		{
			Class<T> clazz = (Class<T>) Class.forName(className);
			T obj = clazz.newInstance();
			obj.configure(configuration);
			return obj;
		}
		catch (Exception ex)
		{
			throw new NioEntityCreationException(ex);
		}
	}
}
