/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.entities;

import io.niowire.NiowireException;

/**
 *
 * @author trent
 */
public class NioEntityCreationException extends NiowireException
{

	public NioEntityCreationException(Exception ex)
	{
		super(ex);
	}
}
