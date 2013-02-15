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

import io.niowire.entities.NioObjectFactory;
import java.util.Map;

/**
 * This class converts a Map object of a particular structure (contains a class
 * and configuration objects) into a {@link NioObjectFactory} class
 *
 * @author Trent Houliston
 */
public class Map2NioObjectFactory implements Converter<Map, NioObjectFactory>
{

	/**
	 * This method converts a Map object containing a class and a configuration
	 * into a {@link NioObjectFactory}
	 *
	 * @param from     the Map to convert from
	 * @param actualTo NioObjectFactory.class
	 *
	 * @return
	 */
	@Override
	public NioObjectFactory convert(Map from, Class<? extends NioObjectFactory> actualTo)
	{
		//Check we have the correct keys
		if (from.containsKey("class") && ((!from.containsKey("configuration"))
										  || (from.containsKey("configuration") && from.get("configuration") instanceof Map)))
		{
			//Build the factory
			return new NioObjectFactory(UniversalConverter.doConvert(from.get("class"), Class.class), (Map<?, ?>) from.get("configuration"));
		}
		else
		{
			//We don't have a class
			throw new UnconvertableObjectException("The passed map did not have a class in it to build the factory from");
		}
	}

	/**
	 * We convert to NioObjectFactory.class
	 *
	 * @return NioObjectFactory.class
	 */
	@Override
	public Class<NioObjectFactory> getTo()
	{
		return NioObjectFactory.class;
	}

	/**
	 * We convert from Map.class
	 *
	 * @return Map.class
	 */
	@Override
	public Class<Map> getFrom()
	{
		return Map.class;
	}

	/**
	 * We are not adaptive (we only convert to NioObjectFactory)
	 *
	 * @return false
	 */
	@Override
	public boolean isAdaptive()
	{
		return false;
	}
}