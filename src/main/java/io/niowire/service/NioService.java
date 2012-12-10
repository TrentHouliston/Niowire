/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.service;

import io.niowire.data.ObjectPacket;
import io.niowire.entities.NioObject;
import io.niowire.server.NioConnection;
import java.util.List;

/**
 *
 * @author trent
 */
public interface NioService extends NioObject
{

	public void setContext(NioConnection.Context context);

	public boolean hasOutput();

	public List<ObjectPacket> recieve();

	public void send(ObjectPacket line);
}
