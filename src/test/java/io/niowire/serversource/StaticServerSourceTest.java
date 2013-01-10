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

import io.niowire.entities.NioObjectFactory;
import io.niowire.service.NioService;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link StaticServerSource}
 *
 * @author Trent Houliston
 */
public class StaticServerSourceTest
{

	/**
	 * Tests All the created server definitions are returned on startup, and
	 * then no more are returned.
	 *
	 * @throws Exception
	 */
	@Test
	public void testStaticServerSource() throws Exception
	{
		LinkedList<NioServerDefinition> defs = new LinkedList<NioServerDefinition>();

		NioServerDefinition def;

		for (int i = 0; i < 5; i++)
		{
			//Create a definition
			def = new NioServerDefinition();
			def.setId("def" + i);
			def.setName("def" + i);
			def.setPort(i);
			def.setInspectorFactory(null);
			def.setSerializerFactory(null);
			def.setServiceFactories(Collections.<NioObjectFactory<NioService>>emptyList());

			//Add it to our lists
			defs.add(def);
		}

		//Create our Server Source
		StaticServerSource source = new StaticServerSource(defs.toArray(new NioServerDefinition[defs.size()]));
		source.configure(Collections.<String, Object>emptyMap());

		//Quick test to make sure that the configuration we get is exactly what we put in
		assertEquals("The returned configuration should be what we put in", source.getConfiguration(), Collections.<String, Object>emptyMap());

		//Get the changes (should be all the servers)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		//Check the returned servers were the right kind
		assertArrayEquals("Not all the servers were returned correctly", servers.keySet().toArray(), defs.toArray());

		//Check that all of the types is SERVER_ADD
		for(Event e : servers.values())
		{
			assertEquals("Not all of the values were a SERVER_ADD", Event.SERVER_ADD, e);
		}

		//Check that if we ask again we get nothing from the source
		assertTrue("There should be no more data in the source", source.getChanges().isEmpty());

		//Close the source
		source.close();
	}
}
