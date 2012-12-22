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
package io.niowire;

import io.niowire.server.NioSocketServer;
import io.niowire.serversource.NioServerSource;

/**
 * This class is the main entry point for the Niowire framework. It is
 * configured by loading a Server Source into it and running start(). This will
 * start up the server with the set configuration as within this configuration
 * is a server definition source which provides definitions to the system. The
 * Niowire server will read these definitions and start up servers to use the
 * servers created from these.
 *
 * @author Trent Houliston
 * @version 1.0.0
 */
public class Niowire
{

	/**
	 * The thread group for Niowire
	 */
	public static final ThreadGroup THREAD_GROUP = new ThreadGroup("Niowire");
	private final NioSocketServer server;
	private final Thread thread;

	/**
	 * Creates a new Niowire thread object to be run using the passed
	 * NioServerSource as it's source.
	 *
	 * @param source the NioServerSource to use in this server
	 *
	 * @throws NiowireException if there was an exception while setting up the
	 *                             server.
	 */
	public Niowire(NioServerSource source) throws NiowireException
	{
		this.server = new NioSocketServer(source);
		this.thread = new Thread(THREAD_GROUP, server, "Niowire");
	}

	/**
	 * Starts up the thread for this Niowire server
	 */
	public void start()
	{
		this.thread.start();
	}

	/**
	 * Shuts down the Niowire server
	 */
	public void shutdown()
	{
		this.server.shutdown();
	}
}
