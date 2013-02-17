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
import io.niowire.inspection.NioAuthenticationException;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.server.NioConnection.Context;
import io.niowire.server.NioSocketServer.ActiveServer;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.service.NioService;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;


import static io.niowire.testutilities.TestUtilities.*;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link NioConnection}
 *
 * @author Trent Houliston
 */
public class NioConnectionTest
{

	private NioConnection connection = null;
	private SelectionKey key = null;
	private NioSerializer serialize = null;
	private NioInspector inspect = null;
	private NioService service = null;
	private NioServerDefinition def = null;
	private ActiveServer activeServer = null;

	/**
	 * Sets up a new Connection object with mocks for most of its components
	 * which will notify when they have been run
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{
		//Create some mock objects
		key = mock(SelectionKey.class);
		serialize = mock(NioSerializer.class);
		inspect = mock(NioInspector.class);
		service = mock(NioService.class);

		//Create some mock factories
		NioObjectFactory<NioSerializer> serializeFactory = mockNioObjectFactory(serialize);
		NioObjectFactory<NioInspector> inspectFactory = mockNioObjectFactory(inspect);
		NioObjectFactory<NioService> serviceFactory = mockNioObjectFactory(service);

		//Create our server definition with all of the passed values
		def = new NioServerDefinition();
		def.setId(DEFAULT_SERVER_ID);
		def.setName(DEFAULT_SERVER_NAME);
		def.setPort(DEFAULT_SERVER_PORT);
		def.setInspectorFactory(inspectFactory);
		def.setSerializerFactory(serializeFactory);
		def.setServiceFactories(Collections.singletonList(serviceFactory));

		activeServer = new ActiveServer(def);

		connection = new NioConnection(key, activeServer);
	}

	/**
	 * This method sets all our local variables to be null
	 */
	@After
	public void teardown()
	{
		this.inspect = null;
		this.key = null;
		this.serialize = null;
		this.service = null;
		this.connection = null;
		this.activeServer = null;
		this.def = null;
	}

	/**
	 * Tests that when the update interest ops method is run, that it correctly
	 * gets the state of the serializer and sets the selection key's operations
	 * to the correct state.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testUpdateInterestOps() throws Exception
	{
		//Check that when we say we have no data that we only have read
		when(serialize.hasData()).thenReturn(false);
		connection.updateInterestOps();
		verify(serialize).hasData();
		verify(key).interestOps(SelectionKey.OP_READ);

		//Check that when we say we have data then we read and write
		when(serialize.hasData()).thenReturn(true);
		connection.updateInterestOps();
		verify(serialize, times(2)).hasData();
		verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		//Check that when we say we have no data again that we go back to reading
		when(serialize.hasData()).thenReturn(false);
		connection.updateInterestOps();
		verify(serialize, times(3)).hasData();
		verify(key, times(2)).interestOps(SelectionKey.OP_READ);
	}

	/**
	 * Tests that when writing data to the connection object, the data flows in
	 * the correct order (serialize to inspector to service) and that the
	 * correct data is passed on.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testWrite() throws Exception
	{
		//Declare our test input
		ByteBuffer input = ByteBuffer.wrap("HELLO!".getBytes("utf-8"));

		//Stub our methods so we get data flow
		when(serialize.deserialize(any(ByteBuffer.class))).thenReturn(Collections.singletonList(BASIC_PACKET));
		when(inspect.inspect(any(NioPacket.class))).thenReturn(MODIFIED_PACKET);

		//Write our input
		connection.write(input);

		//Verify that the data went in the direction we expected
		InOrder order = inOrder(serialize, inspect, service);

		order.verify(serialize).deserialize(input);
		order.verify(inspect).inspect(BASIC_PACKET);
		order.verify(service).send(MODIFIED_PACKET);
	}

	/**
	 * Test that when the read method is run, the data is collected from the
	 * serializer
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testRead() throws Exception
	{
		//We won't have data once we read
		when(serialize.hasData()).thenReturn(false);

		//Create a buffer to hold it
		ByteBuffer buff = ByteBuffer.allocate(10);

		//Read into the buffer
		connection.read(buff);

		//Verify this was passed to the serializer to fill
		verify(serialize).read(buff);

		//Verify that we checked for data
		verify(serialize).hasData();

		//Verify that we no longer have data to read
		verify(key).interestOps(SelectionKey.OP_READ);
	}

	/**
	 * Tests that when data is rebuffered back to the connection that the data
	 * is passed to the serializer
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testRebuffer() throws Exception
	{
		//Create some test data
		ByteBuffer testData = ByteBuffer.wrap("HelloWorld".getBytes("utf-8"));

		//Call Rebuffer
		connection.rebuffer(testData);

		//Verify that the data was sent through
		verify(serialize).rebuffer(testData);
	}

	/**
	 * Tests that when the close method is run then all the components are
	 * closed, and methods now throw closed channel exceptions
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testClose() throws Exception
	{
		//Return a channel that we can close when needed
		when(key.channel()).thenReturn(SocketChannel.open());

		//Close the connection
		connection.close();

		//Verify that we closed all the objects
		verify(serialize).close();
		verify(inspect).close();
		verify(service).close();

		//Assume that if we got the channel it was to close it (cannot verify more as we cannot mock it)
		verify(key).channel();

		//Test that all of the methods are closed and that all of the objects in it have closed

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
		try
		{
			connection.updateInterestOps();
			fail("A ClosedChannelException should have been thrown");
		}
		catch (ClosedChannelException ex)
		{
			assertNotNull(ex);
		}
	}

	/**
	 * Tests that when the Inspector says that the connection should timeout,
	 * that the connection suicides.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testTimeout() throws Exception
	{
		//We want to time out
		when(inspect.timeout()).thenReturn(true);
		when(key.channel()).thenReturn(SocketChannel.open());

		//Execute the timeout method
		connection.timeout();

		//Verify that we ran the timeout method
		verify(inspect).timeout();

		//Verify that we closed all the objects
		verify(serialize).close();
		verify(inspect).close();
		verify(service).close();

		//Assume that if we got the channel it was to close it (cannot verify more as we cannot mock it)
		verify(key).channel();
	}

	/**
	 * Tests that when the timeout returns that we should not timeout that we
	 * don't time out.
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testNoTimeout() throws Exception
	{
		//We don't to time out
		when(inspect.timeout()).thenReturn(false);

		//Execute the timeout method
		connection.timeout();

		//Verify that we ran the timeout method
		verify(inspect).timeout();
	}

	/**
	 * Tests that the connection is closed when the inspector throws an
	 * authentication exception
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testAuthentication() throws Exception
	{
		//Stub our methods so we get data flow
		when(serialize.deserialize(any(ByteBuffer.class))).thenReturn(Collections.singletonList(BASIC_PACKET));
		when(inspect.inspect(any(NioPacket.class))).thenThrow(new NioAuthenticationException());
		when(key.channel()).thenReturn(SocketChannel.open());

		//Make some test data
		ByteBuffer buff = ByteBuffer.allocate(0);

		connection.write(buff);

		InOrder order = inOrder(serialize, inspect);

		order.verify(serialize).deserialize(buff);
		order.verify(inspect).inspect(BASIC_PACKET);

		//Verify that we closed all the objects
		verify(serialize).close();
		verify(inspect).close();
		verify(service).close();

		//Assume that if we got the channel it was to close it (cannot verify more as we cannot mock it)
		verify(key).channel();
	}

	/**
	 * Tests the Context object within the connection
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testContext() throws Exception
	{
		when(inspect.getUid()).thenReturn("TEST_UID");
		when(key.channel()).thenReturn(SocketChannel.open());

		//Get our context
		Context context = connection.getContext();

		//Test the UID
		assertEquals("The UID was not correct", "TEST_UID", context.getUid());
		verify(inspect).getUid();

		//Test the server id
		assertEquals("The Server ID was not correct", DEFAULT_SERVER_ID, context.getServerId());

		//Test the server name
		assertEquals("The Server name was not correct", DEFAULT_SERVER_NAME, context.getServerName());

		//Test the Server Port
		assertEquals("The Server name was not correct", DEFAULT_SERVER_PORT, context.getServerPort());

		//Check that when we say we have no data that we only have read
		when(serialize.hasData()).thenReturn(false);
		context.refreshInterestOps();
		verify(serialize).hasData();
		verify(key).interestOps(SelectionKey.OP_READ);

		//Check that when we say we have data then we read and write
		when(serialize.hasData()).thenReturn(true);
		context.refreshInterestOps();
		verify(serialize, times(2)).hasData();
		verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		//Check that when we say we have no data again that we go back to reading
		when(serialize.hasData()).thenReturn(false);
		context.refreshInterestOps();
		verify(serialize, times(3)).hasData();
		verify(key, times(2)).interestOps(SelectionKey.OP_READ);

		//Write a packet
		context.write(BASIC_PACKET);

		//Verify that it was written to the serializer
		verify(serialize).serialize(BASIC_PACKET);

		//Verify that the interestOps were updated
		verify(key).interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

		//Get the Remote Address
		context.getRemoteAddress();
	}

	/**
	 * Tests updating the servers definition that when the connection is told to
	 * update that it replaces/updates its components
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	@SuppressWarnings("unchecked")
	public void testUpdateServer() throws Exception
	{
		// <editor-fold defaultstate="collapsed" desc="Mock objects">
		//Variables used later
		NioSerializer currentSerializer;
		NioInspector currentInspector;
		List<NioService> currentServices;

		//Mock the serializers to swap around
		NioSerializer[] serializers = new NioSerializer[3];
		serializers[0] = mock(NioSerializer.class);
		serializers[1] = mock(NioSerializer.class);
		serializers[2] = mock(NioSerializer.class);

		//Mock the inspectors to swap around
		NioInspector[] inspectors = new NioInspector[3];
		inspectors[0] = mock(NioInspector.class);
		inspectors[1] = mock(NioInspector.class);
		inspectors[2] = mock(NioInspector.class);

		//Mock the services to swap around
		NioService[] services = new NioService[6];
		services[0] = mock(NioService.class);
		services[1] = mock(NioService.class);
		services[2] = mock(NioService.class);
		services[3] = mock(NioService.class);
		services[4] = mock(NioService.class);
		services[5] = mock(NioService.class);

		//Mock our factories
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioSerializer>[] serializerFactories = (NioObjectFactory<NioSerializer>[]) new NioObjectFactory<?>[3];
		serializerFactories[0] = mockNioObjectFactory(serializers[0]);
		serializerFactories[1] = mockNioObjectFactory(serializers[1]);
		serializerFactories[2] = mockNioObjectFactory(serializers[2]);
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioInspector>[] inspectorFactories = (NioObjectFactory<NioInspector>[]) new NioObjectFactory<?>[3];
		inspectorFactories[0] = mockNioObjectFactory(inspectors[0]);
		inspectorFactories[1] = mockNioObjectFactory(inspectors[1]);
		inspectorFactories[2] = mockNioObjectFactory(inspectors[2]);

		//Mock our service factories
		@SuppressWarnings("unchecked")
		NioObjectFactory<NioService>[] serviceFactories = (NioObjectFactory<NioService>[]) new NioObjectFactory<?>[6];
		serviceFactories[0] = mockNioObjectFactory(services[0]);
		serviceFactories[1] = mockNioObjectFactory(services[1]);
		serviceFactories[2] = mockNioObjectFactory(services[2]);
		serviceFactories[3] = mockNioObjectFactory(services[3]);
		serviceFactories[4] = mockNioObjectFactory(services[4]);
		serviceFactories[5] = mockNioObjectFactory(services[5]);

		//Get the fields we will be looking at
		Field si = connection.getClass().getDeclaredField("serializer");
		si.setAccessible(true);
		Field in = connection.getClass().getDeclaredField("inspect");
		in.setAccessible(true);
		Field sv = connection.getClass().getDeclaredField("services");
		sv.setAccessible(true);
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="First Test (Update Everything)">
		//Update our definition
		def.setSerializerFactory(serializerFactories[0]);
		def.setInspectorFactory(inspectorFactories[0]);
		def.setServiceFactories(Arrays.asList(serviceFactories));

		//Update our active server and definition
		activeServer.update(def);
		connection.updateServerDefinition();

		//Look inside and extract all the values we needed
		currentSerializer = (NioSerializer) si.get(connection);
		currentInspector = (NioInspector) in.get(connection);
		currentServices = (List<NioService>) sv.get(connection);

		//Look inside the connection and make sure that it's serializer/inspector/services are those that are expected
		assertEquals("The serializer was not updated", serializers[0], currentSerializer);
		assertEquals("The inspector was not updated", inspectors[0], currentInspector);
		assertTrue("The services were not updated as expected", Arrays.asList(services).containsAll(currentServices));
		assertTrue("The services were not updated as expected", currentServices.containsAll(Arrays.asList(services)));

		/*
		 * Make sure that the setContext method was called on all these objects.
		 * If this method is called that means that these objects were newly
		 * created.
		 */
		verify(serializerFactories[0]).create(anyMapOf(String.class, Object.class));
		verify(inspectorFactories[0]).create(anyMapOf(String.class, Object.class));
		for (NioObjectFactory<NioService> s : serviceFactories)
		{
			verify(s).create(anyMapOf(String.class, Object.class));
		}

		//Check that the old objects were closed
		verify(serialize).close();
		verify(inspect).close();
		verify(service).close();
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Second Test (Update Inspector, Remove 5 Services)">
		//Update our definition
		def.setSerializerFactory(serializerFactories[0]);
		def.setInspectorFactory(inspectorFactories[1]);
		def.setServiceFactories(Collections.singletonList(serviceFactories[0]));

		//Update our active server and definition
		activeServer.update(def);
		connection.updateServerDefinition();

		//Look inside and extract all the values we needed
		currentSerializer = (NioSerializer) si.get(connection);
		currentInspector = (NioInspector) in.get(connection);
		currentServices = (List<NioService>) sv.get(connection);

		//Look inside the connection and make sure that it's serializer/inspector/services are those that are expected
		assertEquals("The serializer was not updated", serializers[0], currentSerializer);
		assertEquals("The inspector was not updated", inspectors[1], currentInspector);
		assertEquals("The services were not updated as expected", 1, currentServices.size());
		assertTrue("The services were not updated as expected", currentServices.contains(services[0]));

		/*
		 * Make sure that new objects were created from the second factories
		 */
		verify(inspectorFactories[1]).create(anyMapOf(String.class, Object.class));
		verify(serviceFactories[1]).create(anyMapOf(String.class, Object.class));

		//Check that the old objects were closed and that the objects that are staying are not
		verify(inspectors[0]).close();
		for (int i = 1; i < 6; i++)
		{
			verify(services[i]).close();
		}
		//</editor-fold>

		//<editor-fold defaultstate="collapsed" desc="Third Test (Update Serializer & Inspector, Add 2 Services)">
		//Update our definition
		def.setSerializerFactory(serializerFactories[1]);
		def.setInspectorFactory(inspectorFactories[2]);
		def.setServiceFactories(Arrays.asList((NioObjectFactory<NioService>[]) new NioObjectFactory<?>[]
				{
					serviceFactories[0], serviceFactories[1], serviceFactories[2]
				}));

		//Update our active server and definition
		activeServer.update(def);
		connection.updateServerDefinition();

		//Look inside and extract all the values we needed
		currentSerializer = (NioSerializer) si.get(connection);
		currentInspector = (NioInspector) in.get(connection);
		currentServices = (List<NioService>) sv.get(connection);

		//Look inside the connection and make sure that it's serializer/inspector/services are those that are expected
		assertEquals("The serializer was not updated", serializers[1], currentSerializer);
		assertEquals("The inspector was not updated", inspectors[2], currentInspector);
		assertEquals("The services were not updated as expected", 3, currentServices.size());
		assertTrue("The services were not updated as expected", currentServices.containsAll(Arrays.asList(new NioService[]
				{
					services[0], services[1], services[2]
				})));

		/*
		 * Make sure that the new objects were created from the factories
		 */
		verify(inspectorFactories[1]).create(anyMapOf(String.class, Object.class));
		verify(serviceFactories[2], times(2)).create(anyMapOf(String.class, Object.class));

		//Check that the old objects were closed and that the objects that are staying are not
		verify(serializers[0]).close();
		verify(inspectors[1]).close();

		//Verify that our old services closed
		for (int i = 4; i < 6; i++)
		{
			verify(services[i]).close();
		}

		//Verify our new services started
		verify(serviceFactories[1], times(2)).create(anyMapOf(String.class, Object.class));
		verify(serviceFactories[2], times(2)).create(anyMapOf(String.class, Object.class));
		//</editor-fold>

	}
}
