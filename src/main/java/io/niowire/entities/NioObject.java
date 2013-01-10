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

import java.io.Closeable;
import java.util.Map;

/**
 * This interface specifies the minimum requirements to be used as an NioObject
 * (and also be created using reflection by the {@link NioObjectFactory}. To do
 * this it needs to be closeable, and also to accept a configuration.
 *
 * @author Trent Houliston
 */
public interface NioObject extends Closeable
{

	/**
	 * Configure the NioObject with the passed Map, it should use this map to
	 * set itself up. The configuration object should hold information which is
	 * required to do this such as (for example) a character set.
	 *
	 * @param configuration the configuration object which will be used to
	 *                         configure this object once it is constructed
	 *
	 * @throws Exception if there was an exception while configuring
	 */
	public void configure(Map<String, Object> configuration) throws Exception;

	/**
	 * Gets the configuration which was used to setup this object.
	 *
	 * @return the configuration which was used to setup this object.
	 */
	public Map<String, Object> getConfiguration();
}
