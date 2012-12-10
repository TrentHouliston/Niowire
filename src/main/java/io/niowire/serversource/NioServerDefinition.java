/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.serversource;

import io.niowire.entities.NioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.serializer.NioSerializer;
import io.niowire.service.NioService;
import java.util.List;

/**
 *
 * @author trent
 */
public class NioServerDefinition
{

	private String id;
	private String name;
	private int port;
	private NioObjectFactory<NioSerializer> serializerFactory;
	private NioObjectFactory<NioInspector> mangleFactory;
	private List<NioObjectFactory<NioService>> serviceFactories;

	/**
	 * @return the id
	 */
	public String getId()
	{
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(String id)
	{
		this.id = id;
	}

	/**
	 * @return the name
	 */
	public String getName()
	{
		return name;
	}

	/**
	 * @param name the name to set
	 */
	public void setName(String name)
	{
		this.name = name;
	}

	/**
	 * @return the port
	 */
	public int getPort()
	{
		return port;
	}

	/**
	 * @param port the port to set
	 */
	public void setPort(int port)
	{
		this.port = port;
	}

	/**
	 * @return the serializerFactory
	 */
	public NioObjectFactory<NioSerializer> getSerializerFactory()
	{
		return serializerFactory;
	}

	/**
	 * @param serializerFactory the serializerFactory to set
	 */
	public void setSerializerFactory(NioObjectFactory<NioSerializer> serializerFactory)
	{
		this.serializerFactory = serializerFactory;
	}

	/**
	 * @return the mangleFactory
	 */
	public NioObjectFactory<NioInspector> getMangleFactory()
	{
		return mangleFactory;
	}

	/**
	 * @param mangleFactory the mangleFactory to set
	 */
	public void setMangleFactory(NioObjectFactory<NioInspector> mangleFactory)
	{
		this.mangleFactory = mangleFactory;
	}

	/**
	 * @return the serviceFactories
	 */
	public List<NioObjectFactory<NioService>> getServiceFactories()
	{
		return serviceFactories;
	}

	/**
	 * @param serviceFactories the serviceFactories to set
	 */
	public void setServiceFactories(List<NioObjectFactory<NioService>> serviceFactories)
	{
		this.serviceFactories = serviceFactories;
	}

	public void update(NioServerDefinition server)
	{
		//TODO use the properties of this passed in server to update this server object

		//TODO throw an exception of some kind if the changes require a restart
	}
}
