package io.niowire.service;

import io.niowire.data.NioPacket;
import io.niowire.server.NioConnection.Context;
import java.io.IOException;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a simple service which simply echoes whatever it is sent to it back
 * to the client.
 *
 * @author trent
 */
public class EchoService implements NioService
{

	//Our logger
	private static final Logger LOG = LoggerFactory.getLogger(EchoService.class);
	//The context of this connection
	private Context context;
	private Map<String, ? extends Object> configuration;

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
	public void configure(Map<String, ? extends Object> configuration) throws Exception
	{
		this.configuration = configuration;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		//Nothing to clean up
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
	public Map<String, ? extends Object> getConfiguration()
	{
		return configuration;
	}
}
