/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire;

/**
 *
 * @author trent
 */
public class NiowireException extends Exception
{

	public NiowireException()
	{
		super();
	}

	public NiowireException(String message)
	{
		super(message);
	}

	public NiowireException(String message, Exception cause)
	{
		super(message, cause);
	}

	public NiowireException(Exception cause)
	{
		super(cause);
	}
}
