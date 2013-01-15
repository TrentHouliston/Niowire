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
package io.niowire.serversource;

import io.niowire.server.NioSocketServer;

/**
 * This enum is used to describe the state of changes which occur in a server
 * source. It is used to tell the {@link NioSocketServer} if it should be
 * adding/removing or updating the server.
 *
 * @author Trent Houliston
 */
public enum Event
{

	/**
	 * Add a new server to the {@link NioSocketServer}
	 */
	SERVER_ADD,
	/**
	 * Removes a server from the {@link NioSocketServer}
	 */
	SERVER_REMOVE,
	/**
	 * Updates a server in the {@link NioSocketServer}
	 */
	SERVER_UPDATE
}
