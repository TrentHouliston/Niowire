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

import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link DirectoryServerSource}
 *
 * @author Trent Houliston
 */
public class DirectoryServerSourceTest
{

	//Our temporary directory
	private File tempDir;

	/**
	 * This sets up the test directory with 5 server files (all the same)
	 *
	 * @return the directory that contains the server definition files
	 *
	 * @throws Exception
	 */
	@Before
	public void setUp() throws Exception
	{

		//Create a temporary directory
		File tmpdir = File.createTempFile("DirectoryServerSourceTest", "");
		tmpdir.delete();
		tmpdir.mkdir();

		//Make sure it's a file
		assertTrue(tmpdir.isDirectory());

		//Get our test file from our test resources
		String test = new Scanner(DirectoryServerSourceTest.class.getResourceAsStream("test.json")).useDelimiter("\\Z").next();

		//Create an array for our server defintions
		File[] serverDefs = new File[5];

		for (int i = 0; i < serverDefs.length; i++)
		{
			//Create a new file for this server
			serverDefs[i] = new File(tmpdir, Integer.toString(i + 1));

			//Write the test data into the file
			FileWriter fw = new FileWriter(serverDefs[i]);
			fw.write(test);

			//Close it
			fw.close();
		}

		tempDir = tmpdir;
	}

	/**
	 * Delete the temporary folder when we are done with it
	 */
	@After
	public void teardown()
	{
		tempDir.delete();
	}

	/**
	 * Tests that on startup, all of the files in the directory are returned as
	 * server definitions.
	 *
	 * @throws Exception
	 */
	@Test
	public void testDirectoryServerInitialAdd() throws Exception
	{
		//Create our DirectoryServerSource object
		DirectoryServerSource source = new DirectoryServerSource();

		//Use our temporary directory as our source
		source.configure(Collections.singletonMap("directory", (Object) tempDir.getAbsolutePath()));

		//Get the changes (should be all the servers)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		//Check 5 servers were returned
		assertEquals("The wrong number of server definitions were returned", servers.size(), 5);

		//Use bitshifting to work out if all the servers were found
		int found = 0;
		for (Entry<NioServerDefinition, Event> server : servers.entrySet())
		{
			NioServerDefinition key = server.getKey();
			Event value = server.getValue();

			//Bitshift in the server we found in
			switch (key.getId().charAt(0))
			{
				case '1':
					found |= value == Event.SERVER_ADD ? 1 : 0;
					break;
				case '2':
					found |= value == Event.SERVER_ADD ? 2 : 0;
					break;
				case '3':
					found |= value == Event.SERVER_ADD ? 4 : 0;
					break;
				case '4':
					found |= value == Event.SERVER_ADD ? 8 : 0;
					break;
				case '5':
					found |= value == Event.SERVER_ADD ? 16 : 0;
					break;
			}
		}

		//Check that servers 1-5 were found
		assertEquals("Not all the servers were discovered by the source", 1 | 2 | 4 | 8 | 16, found);

		//Close our source
		source.close();
	}

	/**
	 * Tests that when a server definition is updated (newer date modified) it
	 * is returned as an update
	 *
	 * @throws Exception
	 */
	@Test
	public void testUpdateServer() throws Exception
	{
		//Create our DirectoryServerSource object
		DirectoryServerSource source = new DirectoryServerSource();

		//Use our temporary directory as our source
		source.configure(Collections.singletonMap("directory", (Object) tempDir.getAbsolutePath()));

		//Check that 5 servers were returned
		assertEquals("The wrong number of server definitions were returned", 5, source.getChanges().size());

		//Get all the files
		for (File file : tempDir.listFiles())
		{
			//Set their date modified to be 1 second in the future
			file.setLastModified(System.currentTimeMillis() + 1000);
		}

		//Get the changes (should be all the servers as we just updated them all)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		//Use bitshifting to work out if all the servers were found
		int found = 0;
		for (Entry<NioServerDefinition, Event> server : servers.entrySet())
		{
			NioServerDefinition key = server.getKey();
			Event value = server.getValue();

			//Bitshift in the server we found in
			switch (key.getId().charAt(0))
			{
				case '1':
					found |= value == Event.SERVER_UPDATE ? 1 : 0;
					break;
				case '2':
					found |= value == Event.SERVER_UPDATE ? 2 : 0;
					break;
				case '3':
					found |= value == Event.SERVER_UPDATE ? 4 : 0;
					break;
				case '4':
					found |= value == Event.SERVER_UPDATE ? 8 : 0;
					break;
				case '5':
					found |= value == Event.SERVER_UPDATE ? 16 : 0;
					break;
			}
		}

		//Check that servers 1-5 were found
		assertEquals("Not all the servers were discovered by the source", 1 | 2 | 4 | 8 | 16, found);

		//Close our source
		source.close();
	}

	@Test
	public void testDeleteServer() throws Exception
	{
		//Create our DirectoryServerSource object
		DirectoryServerSource source = new DirectoryServerSource();

		//Use our temporary directory as our source
		source.configure(Collections.singletonMap("directory", (Object) tempDir.getAbsolutePath()));

		//Check that 5 servers were returned
		assertEquals("The wrong number of server definitions were returned", 5, source.getChanges().size());

		//Delete all the servers
		for (File file : tempDir.listFiles())
		{
			file.delete();
		}

		//Get the changes (should be all the servers as we just updated them all)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		//Use bitshifting to work out if all the servers were found
		int found = 0;
		for (Entry<NioServerDefinition, Event> server : servers.entrySet())
		{
			NioServerDefinition key = server.getKey();
			Event value = server.getValue();

			//Bitshift in the server we found in
			switch (key.getId().charAt(0))
			{
				case '1':
					found |= value == Event.SERVER_REMOVE ? 1 : 0;
					break;
				case '2':
					found |= value == Event.SERVER_REMOVE ? 2 : 0;
					break;
				case '3':
					found |= value == Event.SERVER_REMOVE ? 4 : 0;
					break;
				case '4':
					found |= value == Event.SERVER_REMOVE ? 8 : 0;
					break;
				case '5':
					found |= value == Event.SERVER_REMOVE ? 16 : 0;
			}
		}

		//Check that servers 1-5 were found
		assertEquals("Not all the servers were discovered by the source", 1 | 2 | 4 | 8 | 16, found);

		//Close our source
		source.close();
	}

	@Test
	public void testAddServer() throws Exception
	{
		//Create our DirectoryServerSource object
		DirectoryServerSource source = new DirectoryServerSource();

		//Use our temporary directory as our source
		source.configure(Collections.singletonMap("directory", (Object) tempDir.getAbsolutePath()));

		//Check that 5 servers were returned
		assertEquals("The wrong number of server definitions were returned", 5, source.getChanges().size());

		//Add 5 more servers
		for (File file : tempDir.listFiles())
		{
			Files.copy(file.toPath(), tempDir.toPath().resolve(Integer.toString(Integer.parseInt(file.getName()) + 5)));
		}

		//Get the changes (should be all the servers as we just updated them all)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		//Use bitshifting to work out if all the servers were found
		int found = 0;
		for (Entry<NioServerDefinition, Event> server : servers.entrySet())
		{
			NioServerDefinition key = server.getKey();
			Event value = server.getValue();

			//Bitshift in the server we found in
			switch (key.getId().charAt(0))
			{
				case '6':
					found |= value == Event.SERVER_ADD ? 1 : 0;
					break;
				case '7':
					found |= value == Event.SERVER_ADD ? 2 : 0;
					break;
				case '8':
					found |= value == Event.SERVER_ADD ? 4 : 0;
					break;
				case '9':
					found |= value == Event.SERVER_ADD ? 8 : 0;
					break;
				case '1':
					//Since the case is only checking for the 1 we need to check for the rest
					if ("10".equals(key.getId()))
					{
						found |= value == Event.SERVER_ADD ? 16 : 0;
					}
			}
		}

		//Check that servers 1-5 were found
		assertEquals("Not all the servers were discovered by the source", 1 | 2 | 4 | 8 | 16, found);

		//Close our source
		source.close();
	}

	/**
	 * This tests that when an invalid server definition is put into the
	 * directory, it is ignored.
	 */
	@Test
	public void testInvalidServerDefinition() throws Exception
	{
		//Create our DirectoryServerSource object
		DirectoryServerSource source = new DirectoryServerSource();

		//Use our temporary directory as our source
		source.configure(Collections.singletonMap("directory", (Object) tempDir.getAbsolutePath()));

		//Check that 5 servers were returned
		assertEquals("The wrong number of server definitions were returned", 5, source.getChanges().size());

		//Invalidate the server configurations
		for (File file : tempDir.listFiles())
		{
			//Modify our existing server to be invalid
			new FileWriter(file, false).append("Totally Invalid Server Specification").close();
			file.setLastModified(System.currentTimeMillis() + 1000);

			//Create new servers that are invalid
			Files.copy(file.toPath(), tempDir.toPath().resolve(Integer.toString(Integer.parseInt(file.getName()) + 5)));
		}

		//Get the changes (since the errors were invalid it should be nothing)
		Map<NioServerDefinition, Event> servers = source.getChanges();

		assertTrue("No changes should have been returned", servers.isEmpty());

		//Close our source
		source.close();
	}
}
