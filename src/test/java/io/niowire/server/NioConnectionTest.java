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
package io.niowire.server;

import io.niowire.data.NioPacket;
import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.service.NioService;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link NioConnection}
 *
 * @author Trent Houliston
 */
public class NioConnectionTest
{

	private static final NioPacket BASE_PACKET = new NioPacket("TEST", "DATA");
	private static final NioPacket MOD_PACKET = new NioPacket("MOD", "MOD_DATA");
	private LinkedList<Event> events = new LinkedList<Event>();
	private NioConnection connection = null;
	private boolean hasData = false;
	private boolean shouldTimeout = false;

	/**
	 * Sets up a new Connection object with mocks for most of its components
	 * which will notify when they have been run
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		//Make a new events object
		events = new LinkedList<Event>();

		//Mock our key
		SelectionKey key = mock(SelectionKey.class);
		when(key.interestOps(any(int.class))).then(new Answer<SelectionKey>()
		{
			@Override
			public SelectionKey answer(InvocationOnMock invocation) throws Throwable
			{
				//Add an event to the queue
				events.add(new Event("key", invocation.getArguments()[0]));

				//Return the key (to fit the API)
				return (SelectionKey) invocation.getMock();
			}
		});
		when(key.channel()).thenReturn(new FakeSelectableChannel());

		//Mock our serializer
		NioSerializer serialize = mock(NioSerializer.class);
		when(serialize.deserialize(any(ByteBuffer.class))).then(new Answer<List<NioPacket>>()
		{
			@Override
			public List<NioPacket> answer(InvocationOnMock invocation) throws Throwable
			{
				//Add an event to the queue
				events.add(new Event("serialize", invocation.getArguments()[0]));

				//Send back a random packet
				return Collections.singletonList(BASE_PACKET);
			}
		});
		when(serialize.hasData()).then(new Answer<Boolean>()
		{
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable
			{
				return hasData;
			}
		});
		doAnswer(new Answer<Object>()
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("serialize", invocation.getArguments()[0]));

				return null;
			}
		}).when(serialize).rebuffer(any(ByteBuffer.class));
		doAnswer(new Answer<Object>()
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("serialize", "close"));

				return null;
			}
		}).when(serialize).close();
		when(serialize.read(any(ByteBuffer.class))).then(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("serialize", "read"));

				return ((ByteBuffer) invocation.getArguments()[0]).remaining();
			}
		});

		//Mock our inspector
		NioInspector inspect = mock(NioInspector.class);
		when(inspect.inspect(any(NioPacket.class))).then(new Answer<NioPacket>()
		{
			@Override
			public NioPacket answer(InvocationOnMock invocation) throws Throwable
			{
				//Add an event to the queue
				events.add(new Event("inspect", invocation.getArguments()[0]));

				return MOD_PACKET;
			}
		});
		when(inspect.timeout()).then(new Answer<Boolean>()
		{
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("inspect", "timeout"));

				return shouldTimeout;
			}
		});
		doAnswer(new Answer<Object>()
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("inspect", "close"));

				return null;
			}
		}).when(inspect).close();

		//Mock our service
		NioService service = mock(NioService.class);
		doAnswer(new Answer<Object>()
		{
			@Override
			public NioPacket answer(InvocationOnMock invocation) throws Throwable
			{
				//Add an event to the queue
				events.add(new Event("service", invocation.getArguments()[0]));

				return null;
			}
		}).when(service).send(any(NioPacket.class));
		doAnswer(new Answer<Object>()
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable
			{
				events.add(new Event("service", "close"));

				return null;
			}
		}).when(service).close();

		//Create some mock factories which simply return the objects
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioSerializer> serializeFactory = mock(NioObjectFactory.class);
		when(serializeFactory.create()).thenReturn(serialize);
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioInspector> inspectFactory = mock(NioObjectFactory.class);
		when(inspectFactory.create()).thenReturn(inspect);
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioService> serviceFactory = mock(NioObjectFactory.class);
		when(serviceFactory.create()).thenReturn(service);

		//Create our server definition with all of the passed values
		NioServerDefinition def = new NioServerDefinition();
		def.setId("Test");
		def.setName("Test");
		def.setPort(Integer.SIZE);
		def.setInspectorFactory(inspectFactory);
		def.setSerializerFactory(serializeFactory);
		def.setServiceFactories(Collections.singletonList(serviceFactory));

		connection = new NioConnection(key, def);
	}

	@Test
	public void testUpdateInterestOps() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;

		//Set hasData to false and update the Interest Ops
		hasData = false;
		connection.getContext().refreshInterestOps();

		//Check our events
		it = events.iterator();
		data = it.next();

		//Check that the key reported that the passed value is OP_READ
		assertEquals("The event should have been from the key", "key", data.source);
		assertEquals("The interest ops should have been read only", SelectionKey.OP_READ, data.data);
		it.remove();

		//That should have been the only event that was fired
		assertTrue("There should be no more events", events.isEmpty());

		//Set hasData to true and update the interest ops
		hasData = true;
		connection.getContext().refreshInterestOps();

		//Check our events
		it = events.iterator();
		data = it.next();

		//Check that the key reported that the passed value is OP_WRITE
		assertEquals("The event should have been from the key", "key", data.source);
		assertEquals("The interest ops should have been read and write", SelectionKey.OP_READ | SelectionKey.OP_WRITE, data.data);
		it.remove();

		//That should have been the only event
		assertFalse("There should be no more events", it.hasNext());

		//Set hasData to false again and update the Interest Ops
		hasData = false;
		connection.getContext().refreshInterestOps();

		//Check our events
		it = events.iterator();
		data = it.next();

		//Check that the key reported that the passed value is OP_READ
		assertEquals("The event should have been from the key", "key", data.source);
		assertEquals("The interest ops should have been read only", SelectionKey.OP_READ, data.data);
		it.remove();

		//That should have been the only event
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * Tests that when writing data to the connection object, the data flows in
	 * the correct order (serialize to inspector to service) and that the
	 * correct data is passed on.
	 *
	 * @throws Exception
	 */
	@Test
	public void testWrite() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;
		ByteBuffer input = ByteBuffer.wrap("HELLO!".getBytes("utf-8"));

		//Write our input
		connection.write(input);

		//Loop through our data
		it = events.iterator();

		//Check that the data first went to the serializer
		data = it.next();
		assertEquals("serialize", data.source);
		assertEquals(input, data.data);
		it.remove();

		//Check that the fake packet from the serializer was then passed to the inspector
		data = it.next();
		assertEquals("inspect", data.source);
		assertEquals(BASE_PACKET, data.data);
		it.remove();

		//Check that then the modified packet was passed to the service
		data = it.next();
		assertEquals("service", data.source);
		assertEquals(MOD_PACKET, data.data);
		it.remove();

		//There should be no more events
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * Test that when the read method is run, the data is collected from the
	 * serializer
	 *
	 * @throws Exception
	 */
	@Test
	public void testRead() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;
		ByteBuffer buff = ByteBuffer.allocate(10);

		connection.read(buff);

		//Check our events
		it = events.iterator();
		data = it.next();

		//Check that the serializer recieved the data
		assertEquals("The event should have been from the serializer", "serialize", data.source);
		assertEquals("The data should have been that we read", "read", data.data);
		it.remove();

		//An Event will have also fired from the key (updating that there is no data to read)
		data = it.next();
		assertEquals("The event should have been from the key", "key", data.source);
		assertEquals("The data should have been that there is no data to write read", SelectionKey.OP_READ, data.data);
		it.remove();

		//That should have been the only event
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * Tests that when data is rebuffered back to the connection that the data
	 * is passed to the serializer
	 *
	 * @throws Exception
	 */
	@Test
	public void testRebuffer() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;
		byte[] testData = "HelloWorld".getBytes("utf-8");

		//Call Rebuffer
		connection.rebuffer(ByteBuffer.wrap(testData));

		//Check our events
		it = events.iterator();
		data = it.next();

		//Check that the serializer recieved the data
		assertEquals("The event should have been from the serializer", "serialize", data.source);
		assertArrayEquals("The data should have been exactly what was in the data", testData, ((ByteBuffer) data.data).array());
		it.remove();

		//That should have been the only event
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * Tests that when the close method is run then all the components are
	 * closed, and methods now throw closed channel exceptions
	 *
	 * @throws Exception
	 */
	@Test
	public void testClose() throws Exception
	{
		//Close the connection
		connection.close();

		//Test that all of the methods are closed and that all of the objects in it have closed
		testClosedMethods();
	}

	/**
	 * Tests that when the Inspector says that the connection should timeout,
	 * that the connection suicides.
	 *
	 * @throws Exception
	 */
	@Test
	public void testTimeout() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;
		shouldTimeout = true;

		//Execute the timeout method
		connection.timeout();

		//Get our first event (the timeout one)
		it = events.iterator();
		data = it.next();

		//Check that the inspector was asked for the timeout
		assertEquals("The event should have been from the inspector", "inspect", data.source);
		assertEquals("The event should have been a timeout call", "timeout", data.data);
		it.remove();

		//Test that the connection has closed down
		testClosedMethods();
	}

	/**
	 * Tests that when the timeout returns that we should not timeout that we
	 * don't time out.
	 *
	 * @throws Exception
	 */
	@Test
	public void testNoTimeout() throws Exception
	{
		//Just check events is empty
		assertTrue("The events should be empty", events.isEmpty());

		//Declare some variables
		Event data;
		Iterator<Event> it;
		shouldTimeout = false;

		//Attempt to timeout the connection
		connection.timeout();

		it = events.iterator();
		data = it.next();

		//Check that the inspector was asked for the timeout
		assertEquals("The event should have been from the inspector", "inspect", data.source);
		assertEquals("The event should have been a timeout call", "timeout", data.data);
		it.remove();

		//Check that the connection is still open
		assertTrue("The connection should still be open", connection.isOpen());

		//That should have been the only event
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * This is a helper method which checks
	 *
	 * @throws Exception
	 */
	private void testClosedMethods() throws Exception
	{
		//Declare some variables
		Event data;
		Iterator<Event> it;

		it = events.iterator();

		//Check that the channel was closed
		data = it.next();
		assertEquals("The channel should have closed", "channel", data.source);
		assertEquals("The channel should have closed", "close", data.data);
		it.remove();

		//Check that the serializer was closed
		data = it.next();
		assertEquals("The serializer should have closed", "serialize", data.source);
		assertEquals("The serializer should have closed", "close", data.data);
		it.remove();

		//Check that the inspector was closed
		data = it.next();
		assertEquals("The inspector should have closed", "inspect", data.source);
		assertEquals("The inspector should have closed", "close", data.data);
		it.remove();

		//Check that the service was closed
		data = it.next();
		assertEquals("The service should have closed", "service", data.source);
		assertEquals("The service should have closed", "close", data.data);
		it.remove();

		/*
		 * Test that the methods now throw closed channel exceptions (as the
		 * resources have been cleaned up and wouldn't work anyway)
		 */
		try
		{
			connection.read(null);
			fail("A ClosedChannelException should have been thrown");
		}
		catch (ClosedChannelException ex)
		{
			assertNotNull(ex);
		}
		try
		{
			connection.write(null);
			fail("A ClosedChannelException should have been thrown");
		}
		catch (ClosedChannelException ex)
		{
			assertNotNull(ex);
		}
		try
		{
			connection.rebuffer(null);
			fail("A ClosedChannelException should have been thrown");
		}
		catch (ClosedChannelException ex)
		{
			assertNotNull(ex);
		}

		//That should have been all the events
		assertFalse("There should be no more events", it.hasNext());
	}

	/**
	 * This is a fake SelectableChannel class to return as Mockito seems unable
	 * to mock one.
	 */
	private class FakeSelectableChannel extends SelectableChannel
	{

		/**
		 * Fake method does nothing
		 */
		@Override
		public SelectorProvider provider()
		{
			return null;
		}

		/**
		 * Fake method does nothing
		 */
		@Override
		public int validOps()
		{
			return 0;
		}

		/**
		 * Fake method does nothing
		 */
		@Override
		public boolean isRegistered()
		{
			return false;
		}

		/**
		 * Fake method does nothing
		 *
		 * @param sel selector
		 */
		@Override
		public SelectionKey keyFor(Selector sel)
		{
			return null;
		}

		/**
		 * Fake method does nothing
		 */
		@Override
		public SelectionKey register(Selector sel, int ops, Object att) throws ClosedChannelException
		{
			return null;
		}

		/**
		 * Fake method does nothing
		 *
		 * @throws IOException
		 */
		@Override
		public SelectableChannel configureBlocking(boolean block) throws IOException
		{
			return null;
		}

		/**
		 * Fake method does nothing
		 */
		@Override
		public boolean isBlocking()
		{
			return false;
		}

		/**
		 * Fake method does nothing
		 */
		@Override
		public Object blockingLock()
		{
			return false;
		}

		/**
		 * Outputs to the events that the channel has been requested to close
		 *
		 * @throws IOException
		 */
		@Override
		protected void implCloseChannel() throws IOException
		{
			events.add(new Event("channel", "close"));
		}
	}

	/**
	 * This is an object for holding the events that the tests generate.
	 */
	private static class Event
	{

		public final String source;
		public final Object data;

		/**
		 * Make a new event to hold the source and data for this object
		 *
		 * @param source the source that this event came from (e.g. serializer,
		 *                     inspector etc.)
		 * @param data   the data attached to this event (either information or
		 *                     a string stating what was done)
		 */
		private Event(String source, Object data)
		{
			this.source = source;
			this.data = data;
		}
	}
}
