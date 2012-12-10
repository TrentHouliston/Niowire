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
 * configured by loading a Configuration into it through the load method and
 * running start(). This will start up the server with the set configuration as
 * within this configuration is a server definition source which provides
 * definitions to the system. The Niowire server will read these definitions and
 * start up servers to use the servers created from these.
 *
 * @author Trent Houliston
 * @version 1.0.0
 */
public class Niowire
{

	/**
	 * The thread group for all of the Niowire threads that are run
	 */
	public static final ThreadGroup THREAD_GROUP = new ThreadGroup("Niowire");
	//True if the server has been configured
	private boolean configured = false;
	//The thread object that the main SocketServer is running in
	private Thread serverThread;

	/**
	 * Load the passed configuration into the system. Only a single
	 * configuration can be loaded into Niowire and this will be the source used
	 * when the server is started up.
	 *
	 * @param source the server source for the Niowire singleton
	 *
	 * @throws NiowireException if components of the configuration are invalid
	 */
	public void load(NioServerSource source) throws NiowireException
	{
		//Check if we have already configured the server
		if (!configured)
		{
			//Create a server using this source
			NioSocketServer server = new NioSocketServer(source);

			//Create a new thread to run the socket server in
			serverThread = new Thread(THREAD_GROUP, server, "SocketServer");

			//We are configured
			configured = true;
		}
		else
		{
			//We can only configure once
			throw new RuntimeNiowireException("A configuration has already been loaded");
		}
	}

	/**
	 * Starts up the Niowire framework using the loaded configuration.
	 */
	public void start()
	{
		//Checks if the server is not configured
		if (!configured)
		{
			//Fail saying that the server is not configured
			throw new RuntimeNiowireException("The Niowire service is not yet configured");
		}
		else
		{
			try
			{
				//Start the thread up
				serverThread.start();
			}
			//This is thrown if thread.start() has already been run
			catch (IllegalThreadStateException ex)
			{
				throw new RuntimeNiowireException("The Niowire service has already been started");
			}
		}
	}

	/**
	 * We do not allow cloning of this object as it is a singleton
	 *
	 * @return does not return
	 *
	 * @throws CloneNotSupportedException
	 */
	@Override
	public Object clone() throws CloneNotSupportedException
	{
		throw new CloneNotSupportedException();
	}

	/**
	 * Private constructor
	 */
	private Niowire()
	{
	}

	/**
	 * Gets the singleton instance of Niowire framework
	 *
	 * @return the singleton instance of the Niowire framework
	 */
	public static Niowire getInstance()
	{
		return Singleton.INSTANCE;
	}

	/**
	 * This class is the holder for the Singleton instance of Niowire
	 */
	private static final class Singleton
	{

		/**
		 * The singleton instance of the Niowire object
		 */
		private static final Niowire INSTANCE = new Niowire();
	}
}
