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
import java.util.Collections;
import java.util.HashSet;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
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
	@Test
	public void testEcho() throws Exception
	{
		//Build some test data
		String[] messages = new String[]
		{
			"A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M"
		};

		//Create and configure a new echo service
		EchoService echo = new EchoService();
		echo.configure(Collections.EMPTY_MAP);

		//Build some hash sets to store our result
		final HashSet<NioPacket> sent = new HashSet<NioPacket>();
		final HashSet<NioPacket> returned = new HashSet<NioPacket>();

		//Mock a context
		NioConnection.Context context = mock(NioConnection.Context.class);
		when(context.getUid()).thenReturn("TEST");

		//Mock our write method so that the objects are put into the hash set
		doAnswer(new Answer()
		{
			@Override
			public Object answer(InvocationOnMock invocation) throws Throwable
			{
				NioPacket packet = (NioPacket) invocation.getArguments()[0];
				returned.add(packet);
				return "Wrote back " + packet;
			}
		}).when(context).write(any(NioPacket.class));

		//Set echo's context as this mocked object
		echo.setContext(context);

		//Loop through our strings
		for (String s : messages)
		{
			//Create a new packet
			NioPacket packet = new NioPacket("TEST", s);

			//Add and send the packet
			sent.add(packet);
			echo.send(packet);
		}

		//Remove all the returned packets from sent
		sent.removeAll(returned);

		//Make sure there are no packets left
		assertTrue(sent.isEmpty());
	}
}
