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

import com.google.gson.Gson;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.inspection.TimeoutInspector;
import io.niowire.serializer.JsonSerializer;
import io.niowire.serializer.NioSerializer;
import io.niowire.service.EchoService;
import io.niowire.service.NioService;
import java.util.List;
import java.util.Scanner;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for {@link NioServerDefinition}
 *
 * @author Trent Houliston
 */
public class NioServerDefinitionTest
{

	/**
	 * Test that NioServerDefinitions are created from JSON properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testJsonCreation() throws Exception
	{
		Gson g = new Gson();

		//Get our configuration file
		String input = new Scanner(NioServerDefinition.class
				.getResourceAsStream(NioServerDefinition.class.getSimpleName() + ".json")).useDelimiter("\\Z").next();

		//Convert it
		NioServerDefinition def = g.fromJson(input, NioServerDefinition.class);

		//Test that all the correct data and factories are in the test object
		assertNull(def.getId());
		assertEquals("test", def.getName());
		assertEquals(12012, (int) def.getPort());

		//Test our serializer
		NioObjectFactory<? extends NioSerializer> serializerFactory = def.getSerializerFactory();
		NioSerializer serializer = serializerFactory.create();
		assertEquals(JsonSerializer.class, serializer.getClass());

		//Test our Inspector
		NioObjectFactory<? extends NioInspector> inspectorFactory = def.getInspectorFactory();
		NioInspector inspector = inspectorFactory.create();
		assertEquals(TimeoutInspector.class, inspector.getClass());

		//Test our service factories (should only be the echo service)
		List<NioObjectFactory<? extends NioService>> serviceFactories = def.getServiceFactories();
		assertEquals(1, serviceFactories.size());
		NioService service = serviceFactories.get(0).create();
		assertEquals(EchoService.class, service.getClass());
	}
}
