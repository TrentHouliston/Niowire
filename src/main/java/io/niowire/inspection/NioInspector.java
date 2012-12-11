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
import io.niowire.entities.NioObject;
import io.niowire.server.NioContextUser;

/**
 * This interface is the NioInspector interface, classes which implement this
 * interface are expected to provide several services when a packet is passed to
 * their inspect method.
 *
 * Primarily they are expected to provide the Unique Identifier for this
 * connection. As they are performing stateful inspection of the packets, This
 * class is in the best position to decide on a unique identifier for the
 * object. This string should be unique as it will be used by other services to
 * identify this connection. It is expected that this ID can change, however it
 * should be done seldomly if possible (e.g. have an initial ID and then create
 * a new one once enough packets to identify the sender have been received)
 *
 * It is also expected to provide timeout functionality through its Timeout
 * method. When this method returns true, the connection will be timed out and
 * closed. This should be based on the amount of time since the last packet was
 * received in the Inspector.
 *
 * They are expected to provide authentication through the use of the
 * {@link NioAuthenticationException}. When each packet comes in they are to
 * inspect it and based on their rules reject the packet with this exception
 * closing the connection.
 *
 * They are expected to provide filtering of packets if needed. This means that
 * some packets will be captured by this interface (by returning null). This way
 * packets can be prevented from reaching the service level.
 *
 * They are expected to provide the ability to mangle packets if needed. This
 * means that packets when they come in can be manipulated and modified (perhaps
 * adding new data or totally changing it) and returning the modified objects to
 * be sent to the service.
 *
 *
 *
 * @author Trent Houliston
 */
public interface NioInspector extends NioObject, NioContextUser
{

	/**
	 * This method should return a Unique string to identify this Connection. It
	 * may change however it should not be done often, once to give an initial
	 * UID before information about the connection is known, and again when
	 * enough packets have been collected to give it a final UID.
	 *
	 * As a starting point, a generator is provided in the {@link UidGenerator}
	 * which uses the IP and Port of the connection to generate a hexadecimal
	 * representation of the combination of the two remote (which should always
	 * be unique).
	 *
	 * @return a unique identifier to identify this connection
	 */
	public String getUid();

	/**
	 * This method is run on every single packet before it is used in the
	 * service. It should check the packets for authentication, filter any
	 * packets that should not be sent to the service, and alter any packets
	 * which need to be altered before sending.
	 *
	 * @param packet the packet to be inspected
	 *
	 * @return a packet to give to the services, or null if this packet should
	 *            be filtered
	 *
	 * @throws NioAuthenticationException if this packet made the connection
	 *                                       fail authentication
	 */
	public NioPacket inspect(NioPacket packet) throws NioAuthenticationException;

	/**
	 * The timeout method should return true when this connection should time
	 * out.
	 *
	 * @return true when this connection should time out, false otherwise
	 */
	public boolean timeout();
}
