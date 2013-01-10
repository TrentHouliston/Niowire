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
package io.niowire.inspection;

import io.niowire.data.NioPacket;
import io.niowire.server.NioConnection;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedChannelException;
import java.util.Collections;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the {@link NullInspector}
 *
 * @author Trent Houliston
 */
public class NullInspectorTest
{

	/**
	 * Test that we get our UID properly from the context
	 */
	@Test(timeout = 1000)
	public void testGetUid() throws Exception
	{
		//Build our NullInspector
		NullInspector inspect = new NullInspector();
		inspect.configure(Collections.<String, Object>emptyMap());

		//Small check for the getConfiguration method
		assertEquals("The inspector needs to return the exact object it was created with", Collections.<String, Object>emptyMap(), inspect.getConfiguration());

		//Mock a remote address in the context
		NioConnection.Context context = mock(NioConnection.Context.class);
		when(context.getRemoteAddress()).thenReturn(new InetSocketAddress("171.205.239.171", 52719));

		//Put this mocked context in
		inspect.setContext(context);

		//Make sure that the inspector gets the address from the context and uses it appropropriatly
		assertEquals("We did not get the expected UID", "ABCDEFABCDEF", inspect.getUid());
	}

	/**
	 * Test that we do nothing with the results
	 */
	@Test(timeout = 1000)
	public void testInspect() throws Exception
	{
		//Test that we do nothing with the results
		//Build our NullInspector
		NullInspector inspect = new NullInspector();
		inspect.configure(Collections.<String, Object>emptyMap());

		//Build a packet
		NioPacket packet = new NioPacket("TEST", "TEST");

		//Make sure it was returned
		assertEquals(packet, inspect.inspect(packet));
	}

	/**
	 * Test that when we close the Inspector, every operation throws a closed
	 * channel exception, and that timeout will return true.
	 */
	@Test(timeout = 1000)
	public void testClose() throws Exception
	{
		//Create a new inspector
		NullInspector inspect = new NullInspector();

		//Mock a remote address in the context
		NioConnection.Context context = mock(NioConnection.Context.class);
		when(context.getRemoteAddress()).thenReturn(new InetSocketAddress("171.205.239.171", 52719));

		//Set the context
		inspect.setContext(context);

		//Close it
		inspect.close();

		//Make sure that a UID is still available
		assertEquals("The UID should still remain", "ABCDEFABCDEF", inspect.getUid());

		//Try inspecting (should throw an exception)
		try
		{
			inspect.inspect(null);
			fail("An exception should have been thrown");
		}
		catch (ClosedChannelException ex)
		{
		}

		//Try to get the timeout (should timeout when it's closed)
		assertTrue("Timeout should return true when the inspector is closed", inspect.timeout());
	}
}
