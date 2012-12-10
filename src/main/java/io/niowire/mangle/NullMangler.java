/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.mangle;

import io.niowire.data.ObjectPacket;
import io.niowire.server.NioConnection.Context;
import io.niowire.service.NioAuthenticationException;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author trent
 */
public class NullMangler implements NioMangle
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
