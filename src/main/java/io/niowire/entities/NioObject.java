/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.entities;

import java.io.Closeable;
import java.util.Map;
import io.niowire.server.NioConnection.Context;

/**
 *
 * @author trent
 */
public interface NioObject extends Closeable
{

	public void configure(Map<String, Object> configuration) throws Exception;
}
