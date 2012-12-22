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

import io.niowire.entities.NioObject;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.server.NioPropertyUnchangableException;
import io.niowire.service.NioService;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link NioServerDefinition}
 *
 * @author Trent Houliston
 */
public class NioServerDefinitionTest
{

	/**
	 * Test that attempting to update a server definition works as expected
	 * (updates all the properties it can and throws an exception if it can't
	 * update them)
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdate() throws Exception
	{
		//Make our server definitions
		NioServerDefinition def1 = new NioServerDefinition();
		NioServerDefinition def2 = new NioServerDefinition();
		NioServerDefinition def3 = new NioServerDefinition();
		NioServerDefinition def4 = new NioServerDefinition();

		//Setup defs 1 and 2 with a compatible change
		//Setup our def1
		def1.setId("def1");
		def1.setName("def1");
		def1.setPort(1000);
		def1.setInspectorFactory(new FakeNioObjectFactory<NioInspector>("def1Inspector"));
		def1.setSerializerFactory(new FakeNioObjectFactory<NioSerializer>("def1Serializer"));
		def1.setServiceFactories(Collections.<NioObjectFactory<NioService>>singletonList(new FakeNioObjectFactory<NioService>("def1Service")));

		//Setup our def2
		def2.setId("def2");
		def2.setName("def2");
		def2.setPort(1000);
		def2.setInspectorFactory(new FakeNioObjectFactory<NioInspector>("def2Inspector"));
		def2.setSerializerFactory(new FakeNioObjectFactory<NioSerializer>("def2Serializer"));
		def2.setServiceFactories(Collections.<NioObjectFactory<NioService>>singletonList(new FakeNioObjectFactory<NioService>("def2Service")));

		//Perform the update
		def1.update(def2);

		//Our ID should not have changed
		assertEquals("The ID was changed when it should not have", "def1", def1.getId());

		//Check that the rest of the updates whent through
		assertEquals("The name was not updated correctly", def2.getName(), def1.getName());
		assertEquals("The port was not updated correctly", def2.getPort(), def1.getPort());
		assertEquals("The Inspector was not updated correctly", def2.getInspectorFactory(), def1.getInspectorFactory());
		assertEquals("The Serializer was not updated correctly", def2.getSerializerFactory(), def1.getSerializerFactory());
		assertEquals("The Service Factories were not updated correctly", def2.getServiceFactories().get(0), def1.getServiceFactories().get(0));

		//Setup our def3 and def4 with an incompatibe change (different ports)
		def1.setId("def3");
		def1.setName("def3");
		def1.setPort(2000);
		def1.setInspectorFactory(new FakeNioObjectFactory<NioInspector>("def3Inspector"));
		def1.setSerializerFactory(new FakeNioObjectFactory<NioSerializer>("def3Serializer"));
		def1.setServiceFactories(Collections.<NioObjectFactory<NioService>>singletonList(new FakeNioObjectFactory<NioService>("def3Service")));

		//Setup our def2
		def2.setId("def4");
		def2.setName("def4");
		def2.setPort(1000);
		def2.setInspectorFactory(new FakeNioObjectFactory<NioInspector>("def4Inspector"));
		def2.setSerializerFactory(new FakeNioObjectFactory<NioSerializer>("def4Serializer"));
		def2.setServiceFactories(Collections.<NioObjectFactory<NioService>>singletonList(new FakeNioObjectFactory<NioService>("def4Service")));

		//Perform the update (should throw an exception)
		try
		{
			def1.update(def2);
			fail("An unchangeable property exception should have been thrown");
		}
		catch (NioPropertyUnchangableException ex)
		{
		}

		//Our ID should not have changed
		assertEquals("The ID was changed when it should not have", "def3", def1.getId());

		//Check that the rest of the updates whent through apart from the port change
		assertEquals("The name was not updated correctly", def2.getName(), def1.getName());
		assertNotEquals("The port updated (should not have been)", def2.getPort(), def1.getPort());
		assertEquals("The Inspector was not updated correctly", def2.getInspectorFactory(), def1.getInspectorFactory());
		assertEquals("The Serializer was not updated correctly", def2.getSerializerFactory(), def1.getSerializerFactory());
		assertEquals("The Service Factories were not updated correctly", def2.getServiceFactories().get(0), def1.getServiceFactories().get(0));
	}

	/**
	 * This is a simple test class for the NioObjectFactoires which is used to
	 * check that one factory is equal to another (we don't care about it's
	 * operation in this test)
	 *
	 * @param <T> the type of object that would be generated (if this was a real
	 *               factory)
	 */
	private static class FakeNioObjectFactory<T extends NioObject> implements NioObjectFactory<T>
	{

		/**
		 * Our ID which we are using to test for equality
		 */
		private final String id;

		/**
		 * Creates a new Factory, the equals method will check for equality in
		 * the id
		 *
		 * @param id the id to check equality against
		 */
		private FakeNioObjectFactory(String id)
		{
			this.id = id;
		}

		/**
		 * This method will always return null (as we don't need to perform the
		 * creation)
		 *
		 * @return null
		 *
		 * @throws NioObjectCreationException
		 */
		@Override
		public T create() throws NioObjectCreationException
		{
			return null;
		}

		/**
		 * Checks for equality of the objects by checking their IDS
		 *
		 * @param obj the object to test against
		 *
		 * @return true if the two objects have equal ids
		 */
		@Override
		public boolean equals(Object obj)
		{
			if (obj instanceof FakeNioObjectFactory)
			{
				return this.id.equals(((FakeNioObjectFactory) obj).id);
			}
			else
			{
				return false;
			}
		}

		/**
		 * Overridden hashCode as it is invalid to override equals and not
		 * hashCode
		 *
		 * @return the hashcode for this object (based on the ID)
		 */
		@Override
		public int hashCode()
		{
			int hash = 7;
			hash = 53 * hash + (this.id != null ? this.id.hashCode() : 0);
			return hash;
		}
	}
}
