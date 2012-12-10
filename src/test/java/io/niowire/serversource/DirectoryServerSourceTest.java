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
