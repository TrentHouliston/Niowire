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

import io.niowire.data.ObjectPacket;
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author trent
 */
public class NullInspector implements NioInspector
{

	private Context context;

	@Override
	public String getUid()
	{
		return context.getRemoteAddress().toString();
	}

	@Override
	public ObjectPacket mangle(ObjectPacket line) throws NioAuthenticationException
	{
		return line;
	}

	@Override
	public boolean timeout()
	{
		//We never time out
		return false;
	}

	@Override
	public void setContext(Context context)
	{
		this.context = context;
	}

	@Override
	public boolean hasOutput()
	{
		//We do nothing
		return false;
	}

	@Override
	public List<ObjectPacket> recieve()
	{
		//Return nothing
		return Collections.EMPTY_LIST;
	}

	@Override
	public void send(ObjectPacket line)
	{
		//Do nothing
	}

	@Override
	public void configure(Map<String, Object> configuration) throws Exception
	{
		//Do nothing
	}

	@Override
	public void close() throws IOException
	{
		//Do nothing
	}
}
