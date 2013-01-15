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
import io.niowire.entities.NioObject;
import io.niowire.server.NioContextUser;

/**
 * The NioService interface defines objects which are to be used as services.
 * These services receive the final data packets from the server. They can then
 * process them as they choose and if they need to write back to the client.
 * They can use context.write() to send the data back.
 *
 * @author Trent Houliston
 */
public interface NioService extends NioObject, NioContextUser
{

	/**
	 * Sends a packet of data to this service for it to process.
	 *
	 * @param packet
	 */
	public void send(NioPacket packet);
}
