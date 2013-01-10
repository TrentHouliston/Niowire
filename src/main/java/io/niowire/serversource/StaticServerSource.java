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
package io.niowire.serversource;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * This class implements a static server source. It is made by loading several
 * server definitions in at construction time, It will then return them all on
 * the first run of getChanges()
 *
 * @author Trent Houliston
 */
public class StaticServerSource implements NioServerSource
{

	private LinkedHashMap<NioServerDefinition, Event> changes;
	private boolean first = true;
	private Map<String, Object> configuration;

	/**
	 * Constructs a StaticServerSource with one or more servers
	 *
	 * @param servers the servers to be added
	 */
	public StaticServerSource(NioServerDefinition... servers)
	{
		//Create a new HashMap to store our changes
		changes = new LinkedHashMap<NioServerDefinition, Event>(servers.length);

		//Loop through our definition
		for (NioServerDefinition def : servers)
		{
			//Add them in as server add events
			changes.put(def, Event.SERVER_ADD);
		}
	}

	/**
	 * Gets the changes that are required for this server
	 *
	 * @return the servers we put in at construction time as add events, and
	 *            then nothing
	 *
	 * @throws IOException
	 */
	@Override
	public Map<NioServerDefinition, Event> getChanges() throws IOException
	{
		//On our first run
		if (first)
		{
			//Return our changes
			first = false;
			LinkedHashMap<NioServerDefinition, Event> temp = changes;
			changes = null;
			return temp;
		}
		else
		{
			//Return an empty map
			return Collections.<NioServerDefinition, Event>emptyMap();
		}
	}

	/**
	 * Configure the source
	 *
	 * @param configuration (no configuration is possible)
	 *
	 * @throws Exception
	 */
	@Override
	public void configure(Map<String, Object> configuration) throws Exception
	{
		this.configuration = configuration;
	}

	/**
	 * Closes the source
	 *
	 * @throws IOException
	 */
	@Override
	public void close() throws IOException
	{
		changes = null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Map<String, Object> getConfiguration()
	{
		return configuration;
	}
}
