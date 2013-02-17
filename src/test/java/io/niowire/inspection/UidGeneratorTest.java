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

import io.niowire.testutilities.TestUtilities;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link UidGenerator}
 *
 * @author Trent Houliston
 */
@RunWith(Parameterized.class)
public class UidGeneratorTest
{

	@Parameterized.Parameter(0)
	public String ip;
	@Parameterized.Parameter(1)
	public int port;
	@Parameterized.Parameter(2)
	public String expected;

	/**
	 * Tests that all of the UID generation methods work as expected
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testAddressToUid() throws Exception
	{
		//Build the UID in a number of ways
		InetAddress inet = InetAddress.getByName(ip);
		InetSocketAddress sAddr = new InetSocketAddress(inet, port);
		String uidFromSocket = UidGenerator.addressToUid(sAddr);
		String uidFromString = UidGenerator.addressToUid(ip, port);
		String uidFromInetPort = UidGenerator.addressToUid(inet, port);
		String uidFromBytes = UidGenerator.addressToUid(inet.getAddress(), port);

		//Check the expected results
		assertEquals(expected, uidFromSocket);
		assertEquals(expected, uidFromString);
		assertEquals(expected, uidFromInetPort);
		assertEquals(expected, uidFromBytes);

		//Get 100% coverage by running the utility constructor
		TestUtilities.runPrivateConstructor(UidGenerator.class);
	}

	/**
	 * This function returns several inputs and outputs to test against
	 *
	 * @return a list of inputs and expected outputs to test against
	 */
	@Parameterized.Parameters
	public static List<?> parameters()
	{
		return Arrays.asList(new Object[][]
				{
					{
						//Testing long hex components (2 hex values)
						"171.205.239.171", 52719, "ABCDEFABCDEF"
					},
					{
						//Testing short hex components (1 hex value)
						"10.0.0.1", 80, "0A0000010050"
					},
					{
						//Testing IPv6 addresses
						"dead:beef:cafe:dead:beef:cafe:dead:beef", 51966, "DEADBEEFCAFEDEADBEEFCAFEDEADBEEFCAFE"
					}
				});

	}
}
