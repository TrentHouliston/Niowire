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
