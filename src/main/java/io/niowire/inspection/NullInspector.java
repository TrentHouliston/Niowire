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
import java.util.Map;

/**
 * This class is an inspector which does not do anything with the incoming data
 * and never times out. It uses the IP/Port of the remote socket to build its
 * UID.
 *
 * @author Trent Houliston
 */
public class NullInspector implements NioInspector
{

	//Our context
	private Context context = null;

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
	public NioPacket inspect(NioPacket line) throws NioAuthenticationException
	{
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
		//We never time out
		return false;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void setContext(Context context)
	{
		this.context = context;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void configure(Map<String, Object> configuration) throws Exception
	{
		//Do nothing
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		//Do nothing
	}
}
