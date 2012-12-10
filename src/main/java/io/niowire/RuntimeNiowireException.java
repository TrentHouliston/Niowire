/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire;

/**
 *
 * @author trent
 */
public class RuntimeNiowireException extends RuntimeException
{

	public RuntimeNiowireException()
	{
		super();
	}

	public RuntimeNiowireException(String message)
	{
		super(message);
	}

	public RuntimeNiowireException(String message, Exception cause)
	{
		super(message, cause);
	}

	public RuntimeNiowireException(Exception cause)
	{
		super(cause);
	}
}
