/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.entities;

import java.util.Map;

/**
 *
 * @author trent
 */
public class NioObjectFactory<T extends NioObject>
{

	private final String className;
	private final Map<String, Object> configuration;

	public NioObjectFactory(String className, Map<String, Object> configuration)
	{
		this.className = className;
		this.configuration = configuration;
	}

	public T create() throws NioEntityCreationException
	{
		try
		{
			Class<T> clazz = (Class<T>) Class.forName(className);
			T obj = clazz.newInstance();
			obj.configure(configuration);
			return obj;
		}
		catch (Exception ex)
		{
			throw new NioEntityCreationException(ex);
		}
	}
}
