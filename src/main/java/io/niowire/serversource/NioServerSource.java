/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.serversource;

import java.io.IOException;
import java.util.Map;
import io.niowire.entities.NioObject;
import io.niowire.entities.NioObjectFactory;

/**
 *
 * @author trent
 */
public interface NioServerSource extends NioObject
{

	public Map<NioServerDefinition, Event> getChanges() throws IOException;
}
