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
package io.niowire.service;

import io.niowire.data.NioPacket;
import io.niowire.server.NioConnection;
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import org.junit.Test;
import org.mockito.InOrder;

import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link EchoService}
 *
 * @author Trent Houliston
 */
public class EchoServiceTest
{

	/**
	 * Tests the operation of the EchoService (checks that every packet it is
	 * given it returns)
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testEcho() throws Exception
	{
		//Build some test data
		NioPacket[] packets = new NioPacket[]
		{
			new NioPacket("Test", "A"),
			new NioPacket("Test", "B"),
			new NioPacket("Test", "C"),
			new NioPacket("Test", "D"),
			new NioPacket("Test", "E"),
			new NioPacket("Test", "F"),
			new NioPacket("Test", "G"),
			new NioPacket("Test", "H"),
			new NioPacket("Test", "I"),
			new NioPacket("Test", "J"),
			new NioPacket("Test", "K"),
			new NioPacket("Test", "L"),
			new NioPacket("Test", "M")
		};

		//Create and configure a new echo service
		EchoService echo = new EchoService();

		//Mock a context
		NioConnection.Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Set echo's context as this mocked object
		NioConnection.injectContext(echo, context);

		//Loop through our strings
		for (NioPacket p : packets)
		{
			//send the packet
			echo.send(p);
		}

		//Verify that the packets are written back in order
		InOrder order = inOrder(context);
		for (NioPacket packet : packets)
		{
			order.verify(context).write(packet);
		}

		//Check that that was all that was sent
		order.verifyNoMoreInteractions();
	}

	/**
	 * Tests that when an exception is thrown while writing that the packet is
	 * dropped and the service keeps running. This test fails if there is an
	 * exception
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testExceptionOnWrite() throws Exception
	{
		Context mock = mock(NioConnection.Context.class);
		doThrow(new IOException()).when(mock).write(any(NioPacket.class));

		EchoService service = new EchoService();

		NioConnection.injectContext(service, mock);
		service.send(new NioPacket("Test", "Test"));
	}

	/**
	 * Executes the unused methods so that they have code coverage
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testUnusedMethods() throws Exception
	{
		EchoService service = new EchoService();
		service.close();
	}
}
