package io.niowire.inspection;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

/**
 * This class generates a Uid from an IP/Port combination
 *
 * @author Trent Houliston
 */
public class UidGenerator
{

	/**
	 * This method builds a UID from a InetSocketAddress
	 *
	 * @param remoteAddress the remote address to build a UID for
	 *
	 * @return a String containing the hex representation of the address and
	 *            port
	 */
	public static String addressToUid(InetSocketAddress remoteAddress)
	{
		//Split up the address and call the other method
		return addressToUid(remoteAddress.getAddress(), remoteAddress.getPort());
	}

	/**
	 * This method converts an IP and port into a UID
	 *
	 * @param address the address to convert
	 * @param port    the port to convert
	 *
	 * @return a String containing the hex representation of the address and
	 *            port
	 */
	public static String addressToUid(InetAddress address, int port)
	{
		return addressToUid(address.getAddress(), port);
	}

	/**
	 * This method converts an IP and port into a UID
	 *
	 * @param address the address to convert as a string
	 * @param port    the port to convert
	 *
	 * @return a String containing the hex representation of the address and
	 *            port
	 */
	public static String addressToUid(String address, int port) throws UnknownHostException
	{
		return addressToUid(InetAddress.getByName(address), port);
	}

	/**
	 * This method converts an IP and port into a UID
	 *
	 * @param address the the address to convert in byte form
	 * @param port    the port to convert
	 *
	 * @return a String containing the hex representation of the address and
	 *            port
	 */
	public static String addressToUid(byte[] address, int port)
	{
		//Get a new string builder
		StringBuilder str = new StringBuilder();

		//Loop through our bytes (will be a differnt number for IPv4 vs IPV6
		for (byte b : address)
		{
			//Append each byte as hex
			str.append(String.format("%02x", b));
		}

		//Append the port as hex
		str.append(String.format("%04x", port));

		//Return the value
		return str.toString();
	}

	/**
	 * Private constructor as we are a Utility class
	 */
	private UidGenerator()
	{
	}
}
