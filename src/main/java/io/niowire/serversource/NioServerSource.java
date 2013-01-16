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
import java.io.Closeable;
import java.io.IOException;
import java.util.Map;

/**
 * This interface defines a server source. A server source will monitor a list
 * of server defintions and return any changes to this list. It will
 * add/remove/update any server within its scope and the {@link NioSocketServer}
 * will use these changes to maintain it's state.
 *
 * @author Trent Houliston
 */
public interface NioServerSource extends Closeable
{

	/**
	 * Gets a map of server definition objects and the changes which have been
	 * made to them.
	 *
	 * @return a map of ServerDefinitions and changes that have happened to them
	 *
	 * @throws IOException          if there is an IOException from the source
	 *                                 of these servers
	 * @throws InterruptedException if this is a blocking server source and the
	 *                                 thread was interrupted
	 */
	public Map<NioServerDefinition, Event> getChanges() throws IOException, InterruptedException;

	/**
	 * This method checks if a source is blocking. If it is then the
	 * SourceRunner which is monitoring it will not wait between fetches of the
	 * servers.
	 *
	 * @return if the server source is blocking
	 */
	public boolean isBlocking();
}
