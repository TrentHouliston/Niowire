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

import java.net.InetAddress;
import java.net.InetSocketAddress;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link UidGenerator}
 *
 * @author Trent Houliston
 */
public class UidGeneratorTest
{

	/**
	 * Test the method which accepts a socket address
	 */
	@Test
	public void testAddressToUid() throws Exception
	{
		InetAddress inet;
		InetSocketAddress sAddr;
		String uid;

		//Test UIDS with long components (2 hex characters)
		inet = InetAddress.getByName("171.205.239.171");
		sAddr = new InetSocketAddress(inet, 52719);
		uid = UidGenerator.addressToUid(sAddr);
		assertEquals("ABCDEFABCDEF", uid);

		//Test Uids with short components (1 hex character)
		inet = InetAddress.getByName("10.0.0.1");
		sAddr = new InetSocketAddress(inet, 80);
		uid = UidGenerator.addressToUid(sAddr);
		assertEquals("0A0000010050", uid);

		//Test IPv6 addresses
		inet = InetAddress.getByName("dead:beef:cafe:dead:beef:cafe:dead:beef");
		sAddr = new InetSocketAddress(inet, 51966);
		uid = UidGenerator.addressToUid(sAddr);
		assertEquals("DEADBEEFCAFEDEADBEEFCAFEDEADBEEFCAFE", uid);
	}
}
