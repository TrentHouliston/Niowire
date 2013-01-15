/**
 * This file is part of Niowire.
 *
 * Niowire is free software: you can redistribute it and/or modify it under the
 * terms of the Lesser GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * Niowire is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the Lesser GNU General Public License for more
 * details.
 *
 * You should have received a copy of the Lesser GNU General Public License
 * along with Niowire. If not, see <http://www.gnu.org/licenses/>.
 */
package io.niowire.serversource;

import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.service.NioService;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * This class describes a server definition object. It describes a socket server
 * details and the processing which will be required when a connection is made
 * ({@link NioSerializer}, {@link NioInspector}, {@link NioService}).
 *
 * @author Trent Houliston
 */
public class NioServerDefinition
{

	//Our variables
	protected String id;
	protected String name;
	//Box the port so it can be null (grab any free port)
	protected Integer port;
	protected NioObjectFactory<NioSerializer> serializerFactory;
	protected NioObjectFactory<NioInspector> inspectorFactory;
	protected List<NioObjectFactory<NioService>> serviceFactories;

	/**
	 * Gets the ID (unique identifier) for this server
	 *
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * Sets the ID (unique identifier) for this server
	 *
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * Gets the declared name for this server
	 *
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * Sets the declared name for this server
	 *
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * Gets the port to listen on for this server
	 *
	 * @return the port
	 */
	public Integer getPort()
	{
		return port;
	}

	/**
	 * Sets the port to listen on for this server
	 *
	 * @param port the port to set
	 */
	public void setPort(Integer port)
	{
		this.port = port;
	}

	/**
	 * Gets the serializer factory for this source
	 *
	 * @return the serializerFactory
	 */
	public NioObjectFactory<NioSerializer> getSerializerFactory()
	{
		return serializerFactory;
	}

	/**
	 * Sets the serializer factory for this source
	 *
	 * @param serializerFactory the serializerFactory to set
	 */
	public void setSerializerFactory(NioObjectFactory<NioSerializer> serializerFactory)
	{
		this.serializerFactory = serializerFactory;
	}

	/**
	 * Gets the inspector factory for this source
	 *
	 * @return the inspectorFactory
	 */
	public NioObjectFactory<NioInspector> getInspectorFactory()
	{
		return inspectorFactory;
	}

	/**
	 * Sets the inspector factory for this source
	 *
	 * @param inspectorFactory the inspectorFactory to set
	 */
	public void setInspectorFactory(NioObjectFactory<NioInspector> inspectorFactory)
	{
		this.inspectorFactory = inspectorFactory;
	}

	/**
	 * Gets a list of service factories for this source
	 *
	 * @return the serviceFactories
	 */
	public List<NioObjectFactory<NioService>> getServiceFactories()
	{
		//Make it unmodifiable
		return Collections.unmodifiableList(serviceFactories);
	}

	/**
	 * Sets a list of service factories for this source
	 *
	 * @param serviceFactories the serviceFactories to set
	 */
	public void setServiceFactories(List<? extends NioObjectFactory<NioService>> serviceFactories)
	{
		//Wrap it so that nobody else has access to our shared state
		this.serviceFactories = new LinkedList<NioObjectFactory<NioService>>(serviceFactories);
	}
}
