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
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import javax.inject.Inject;

/**
 * This is a simple Inspector which will timeout after a message has not been
 * received for the amount of time that it was configured with
 *
 * @author Trent Houliston
 */
public class TimeoutInspector implements NioInspector
{

	//Our context
	@Inject
	private Context context = null;
	private long lastMessage = System.currentTimeMillis();
	@Inject
	private long timeout;
	private boolean open = true;

	/**
	 * We use the UID generator to generate our UID based on the remoteAddress
	 * socket
	 *
	 * @return the UID
	 */
	@Override
	public String getUid()
	{
		return UidGenerator.addressToUid(context.getRemoteAddress());
	}

	/**
	 * We do nothing while inspecting, we just return whatever we are given
	 *
	 * @param line the line we are inspecting (just returned)
	 *
	 * @return the line we were given
	 *
	 * @throws NioAuthenticationException does not get thrown ever in this class
	 */
	@Override
	public NioPacket inspect(NioPacket line) throws NioAuthenticationException, IOException
	{
		if (!open)
		{
			throw new ClosedChannelException();
		}
		lastMessage = System.currentTimeMillis();
		return line;
	}

	/**
	 * Returns if we should time out or not
	 *
	 * @return false
	 */
	@Override
	public boolean timeout()
	{
		//Timeout when the inspector is closed
		return !open || ((timeout > 0) && (System.currentTimeMillis() - lastMessage > timeout));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		open = false;
	}
}
