package io.niowire.serversource;

import org.junit.Test;

/**
 *
 * @author trent
 */
public class DirectoryServerSourceTest
{
	@Test
	public void testDirectoryServerInit()
	{
		//TODO test that on startup it will return all the configs in a directory
	}

	@Test
	public void testUpdateServer()
	{
		//TODO test that when a server file is touched (modified) it is returned
	}

	@Test
	public void testDeleteServer()
	{
		//TODO test that when a server file is deleted, the fact it is deleted is returned
	}

	@Test
	public void testAddServer()
	{
		//TODO test that when a server file is added the fact it is added is returned
	}
}
