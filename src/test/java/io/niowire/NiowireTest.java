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
package io.niowire;

import com.google.gson.Gson;
import io.niowire.entities.ReflectiveNioObjectFactory;
import io.niowire.inspection.NioInspector;
import io.niowire.inspection.NullInspector;
import io.niowire.serializer.JsonSerializer;
import io.niowire.serializer.NioSerializer;
import io.niowire.serversource.NioServerDefinition;
import io.niowire.serversource.StaticServerSource;
import io.niowire.service.EchoService;
import io.niowire.service.NioService;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Collections;
import java.util.Scanner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * This is one of the major Integration tests for Niowire it starts up a JSON
 * parsing server with an echo service and ensures that the data that goes to
 * and from it is what is expected.
 *
 * @author Trent Houliston
 */
public class NiowireTest
{

	private NioServerDefinition def = null;
	private Niowire niowire = null;

	@Before
	public void setUp() throws Exception
	{
		def = new NioServerDefinition();

		//Set our name and ID
		def.setId("TestServer");
		def.setName("TestServer");

		//Set a null port so we find a random free one (it will then be set into this)
		def.setPort(null);
		ReflectiveNioObjectFactory<NioInspector> inspect = new ReflectiveNioObjectFactory<NioInspector>(NullInspector.class.getName(), Collections.<String, Object>emptyMap());
		ReflectiveNioObjectFactory<NioSerializer> serialize = new ReflectiveNioObjectFactory<NioSerializer>(JsonSerializer.class.getName(), Collections.singletonMap("charset", "utf-8"));
		ReflectiveNioObjectFactory<NioService> service = new ReflectiveNioObjectFactory<NioService>(EchoService.class.getName(), Collections.<String, Object>emptyMap());

		def.setInspectorFactory(inspect);
		def.setSerializerFactory(serialize);
		def.setServiceFactories(Collections.singletonList(service));

		StaticServerSource source = new StaticServerSource(def);

		niowire = new Niowire(source);

		niowire.start();
	}

	@After
	public void teardown()
	{
		niowire.shutdown();
	}

	@Test
	public void testServerOperations() throws Exception
	{
		//Create a gson for communication
		Gson g = new Gson();

		//Wait for the server to set itself up
		while (def.getPort() == null)
		{
			Thread.sleep(10);
		}

		//Get the port now that it's a real port
		int port = def.getPort();

		//Connect to the server
		Socket s = new Socket("localhost", port);

		//Setup our input and output streams
		PrintWriter out = new PrintWriter(new OutputStreamWriter(s.getOutputStream(), "utf-8"));
		Scanner in = new Scanner(s.getInputStream(), "utf-8");

		//Create an array of numbers to send
		String[] data = new String[]
		{
			"Hello", "World", "How", "Are", "You"
		};

		out.write(g.toJson(data) + "\n");
		out.flush();

		String line = in.nextLine();
		String[] back = g.fromJson(line, String[].class);

		assertArrayEquals("The send and recieved arrays were not correct", data, back);

		out.close();
		in.close();
		s.close();
	}
}
