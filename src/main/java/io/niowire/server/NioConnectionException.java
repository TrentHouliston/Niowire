/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.server;

import io.niowire.NiowireException;

/**
 *
 * @author trent
 */
public class NioConnectionException extends NiowireException
{

	public NioConnectionException()
	{
		super();
	}

	public NioConnectionException(String message, Exception cause)
	{
		super(message, cause);
	}

	public NioConnectionException(Exception cause)
	{
		super(cause);
	}
}
