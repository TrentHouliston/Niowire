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
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import javax.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple service which simply echoes whatever it is sent to it back
 * to the client.
 *
 * @author Trent Houliston
 */
public class EchoService implements NioService
{

	//Our logger
	private static final Logger LOG = LoggerFactory.getLogger(EchoService.class);
	//The context of this connection
	@Inject
	private Context context;

	/**
	 * Gets a packet of data to process from the client. It will be returned
	 * straight back to them.
	 *
	 * @param packet the packet to send
	 */
	@Override
	public void send(NioPacket packet)
	{
		try
		{
			//Send the packet straight to be written
			context.write(packet);
		}
		catch (IOException ex)
		{
			LOG.warn("An exception occured in the EchoService", ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		//Nothing to clean up
	}
}
