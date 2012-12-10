/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.mangle;

import io.niowire.data.ObjectPacket;
import io.niowire.service.NioAuthenticationException;
import io.niowire.service.NioService;

/**
 *
 * @author trent
 */
public interface NioMangle extends NioService
{

	public String getUid();

	public ObjectPacket mangle(ObjectPacket line) throws NioAuthenticationException;

	public boolean timeout();
}
