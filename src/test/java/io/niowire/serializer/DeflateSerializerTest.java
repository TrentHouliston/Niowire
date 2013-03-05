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
package io.niowire.serializer;

import io.niowire.data.NioPacket;
import io.niowire.entities.Injector;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection.Context;
import io.niowire.testutilities.TestUtilities;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link DeflateSerializer}
 *
 * @author Trent Houliston
 */
public class DeflateSerializerTest
{

	private DeflateSerializer serializer;
	private NioSerializer internal;
	private Context context;
	private static byte[] compressed;
	private static byte[] uncompressed;

	/**
	 * This method reads in the example compressed and uncompressed files so
	 * that they can be tested through the system.
	 *
	 * @throws Exception
	 */
	@BeforeClass
	public static void getExampleData() throws Exception
	{
		//Make a transfer buffer
		byte[] transferBuffer = new byte[2048];
		int read = 0;

		//Get our compressed file
		InputStream compressedInputStream = DeflateSerializerTest.class.getResourceAsStream(DeflateSerializerTest.class.getSimpleName() + ".txt.deflate");
		ByteArrayOutputStream compressedBos = new ByteArrayOutputStream();
		while ((read = compressedInputStream.read(transferBuffer)) != -1)
		{
			compressedBos.write(transferBuffer, 0, read);
		}

		compressedInputStream.close();

		InputStream uncompressedInputStream = DeflateSerializerTest.class.getResourceAsStream(DeflateSerializerTest.class.getSimpleName() + ".txt");
		ByteArrayOutputStream uncompressedBos = new ByteArrayOutputStream();
		while ((read = uncompressedInputStream.read(transferBuffer)) != -1)
		{
			uncompressedBos.write(transferBuffer, 0, read);
		}

		//Store our values
		compressed = compressedBos.toByteArray();
		uncompressed = uncompressedBos.toByteArray();

		uncompressedInputStream.close();
	}

	/**
	 * Builds our serializer/context details for each test
	 *
	 * @throws Exception
	 */
	@Before
	public void setup() throws Exception
	{
		//Mock our internal serializer
		internal = mock(NioSerializer.class);
		NioObjectFactory<NioSerializer> factory = TestUtilities.mockNioObjectFactory(internal);
		context = mock(Context.class);

		//For our pretend input
		final ByteBuffer data = ByteBuffer.wrap(uncompressed);

		//When deserializing just return a packet with the bytes it was called with
		when(internal.deserialize(any(ByteBuffer.class))).then(new Answer<List<NioPacket>>()
		{
			@Override
			public List<NioPacket> answer(InvocationOnMock invocation) throws Throwable
			{
				ByteBuffer input = (ByteBuffer) invocation.getArguments()[0];
				byte[] data = new byte[input.remaining()];
				input.get(data);

				return Collections.singletonList(new NioPacket("Test", data));
			}
		});

		//When reading then return the uncompressed data from our final buffer
		when(internal.read(any(ByteBuffer.class))).then(new Answer<Integer>()
		{
			@Override
			public Integer answer(InvocationOnMock invocation) throws Throwable
			{
				//Get the target buffer
				ByteBuffer buff = (ByteBuffer) invocation.getArguments()[0];

				if (buff.remaining() < data.remaining())
				{
					//Transfer all the data we can in
					return DelimitedSerializer.transferMax(data, buff);
				}
				else
				{
					//Transfer all the data in
					int remaining = data.remaining();
					buff.put(data);
					return remaining;
				}
			}
		});

		//When checking for hasData just check if there is data remaining in our buffer
		when(internal.hasData()).then(new Answer<Boolean>()
		{
			@Override
			public Boolean answer(InvocationOnMock invocation) throws Throwable
			{
				return data.hasRemaining();
			}
		});

		//Create a serializer
		serializer = new DeflateSerializer();

		//Build our conifguration
		HashMap<String, Object> config = new HashMap<String, Object>();
		config.put("serializer", factory);
		config.put("compressionLevel", 9);

		//Inject our details
		Injector<DeflateSerializer> inject = new Injector<DeflateSerializer>(DeflateSerializer.class, config);
		inject.inject(serializer, Collections.singletonMap("context", context));
	}

	/**
	 * Tests that the deserializer is correctly deflating the passed data before
	 * sending it to the internal serializer.
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeserialize() throws Exception
	{
		//Wrap our compressed array in a buffer
		ByteBuffer in = ByteBuffer.wrap(compressed);

		//Get our packets
		List<NioPacket> packets = serializer.deserialize(in);
		//Check that we got the same as the uncompressed file
		assertArrayEquals("The data that was decompressed did not equal the decompressed file", uncompressed, bytesFromPackets(packets));
	}

	/**
	 * Tests that multiple parts of compressed data are deserialized properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testMultiPartDeserialize() throws Exception
	{
		int parts = 4;

		//Wrap our compressed array in a buffer
		ByteBuffer in = ByteBuffer.wrap(compressed);


		//Split up our in buffer into multiple seperate parts
		ByteBuffer[] inputBuffer = new ByteBuffer[parts];
		for (int i = 0; i < parts; i++)
		{
			//Split the buffer up
			in.position(i * (in.capacity() / parts));
			in.limit((i + 1) * (in.capacity() / parts));
			inputBuffer[i] = in.slice();
		}

		LinkedList<NioPacket> packets = new LinkedList<NioPacket>();
		for (ByteBuffer buffer : inputBuffer)
		{
			packets.addAll(serializer.deserialize(buffer));
		}

		//Check that we got the same as the uncompressed file
		assertArrayEquals("The data that was decompressed did not equal the decompressed file", uncompressed, bytesFromPackets(packets));
	}

	/**
	 * Tests that data entered by the serialize method is able to be read back
	 * and that it is compressed
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testRead() throws Exception
	{
		//Allocating way more then we need here
		ByteBuffer buff = ByteBuffer.allocate(uncompressed.length);

		//Read into the buffer
		serializer.read(buff);
		buff.flip();

		/*
		 * As we have tested that the deserialize method works (through another
		 * unit test), we can use it to verify that the data was compressed
		 * correctly (by decompressing it)
		 */
		List<NioPacket> result = serializer.deserialize(buff);
		assertArrayEquals("The data which resulted from the decompression was wrong", uncompressed, bytesFromPackets(result));
	}

	/**
	 * Tests that for large packets being read out, the data is split up and
	 * returned in chunks
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testSplitRead() throws Exception
	{
		//Allocating way less then we should here
		ByteBuffer buff = ByteBuffer.allocate(50);

		//Allocate somewhere to store the final data
		ByteBuffer data = ByteBuffer.allocate(uncompressed.length);

		while (serializer.hasData())
		{
			//Read into the buffer
			buff.clear();
			serializer.read(buff);
			buff.flip();
			data.put(buff);
		}
		data.flip();

		/*
		 * As we have tested that the deserialize method works (through another
		 * unit test), we can use it to verify that the data was compressed
		 * correctly (by decompressing it)
		 */
		List<NioPacket> result = serializer.deserialize(data);
		assertArrayEquals("The data which resulted from the decompression was wrong", bytesFromPackets(result), uncompressed);
	}

	/**
	 * Tests that unused data can be returned to the serializer to be
	 * re-buffered and that it handles the compression properly
	 *
	 * @throws Exception
	 */
	@Test(timeout = 1000)
	public void testDataReturn() throws Exception
	{
		//Allocating way more then we need as well as a small buffer
		ByteBuffer buff1 = ByteBuffer.allocate(20);
		ByteBuffer buff2 = ByteBuffer.allocate(uncompressed.length);

		//Read into the buffer
		serializer.read(buff1);
		buff1.flip();

		//Rebuffer the data
		serializer.rebuffer(buff1);

		//Now try to read everything into the larger buffer
		serializer.read(buff2);
		buff2.flip();

		/*
		 * As we have tested that the deserialize method works (through another
		 * unit test), we can use it to verify that the data was compressed
		 * correctly (by decompressing it)
		 */
		List<NioPacket> result = serializer.deserialize(buff2);
		assertArrayEquals("The data which resulted from the decompression was wrong", uncompressed, bytesFromPackets(result));
	}

	/**
	 * Tests that this class can be correctly created using Json
	 *
	 * @throws Exception
	 */
	@Test//(timeout = 1000)
	public void testJsonCreation() throws Exception
	{
		//Get an array of lines (our output strings
		String[] lines = new String(uncompressed, Charset.defaultCharset()).replaceAll("\r", "").split("\n");
		//<editor-fold defaultstate="collapsed" desc="Test Decompression">
		{
			//Get a serializer from Json
			serializer = TestUtilities.buildAndTestFromJson(DeflateSerializer.class, context);

			//Wrap our compressed data
			ByteBuffer input = ByteBuffer.wrap(compressed);

			List<NioPacket> data = serializer.deserialize(input);

			//There check there are the right number of lines returned
			assertEquals("There were the wrong number of lines returned", lines.length, data.size());

			for (int i = 0; i < data.size(); i++)
			{
				assertEquals("The line was not correctly decompressed", lines[i], data.get(i).getData());
			}
		}
		//</editor-fold>
		//<editor-fold defaultstate="collapsed" desc="Test Compression">
		{
			//Get a serializer from Json
			serializer = TestUtilities.buildAndTestFromJson(DeflateSerializer.class, context);

			//Serialize in every line as a packet
			for (String line : lines)
			{
				serializer.serialize(new NioPacket("Test", line));
			}

			//Read the data into a buffer (which is much larger then needed)
			ByteBuffer serialized = ByteBuffer.allocate(uncompressed.length);
			serializer.read(serialized);
			serialized.flip();

			//Deserialize this buffer
			List<NioPacket> data = serializer.deserialize(serialized);
			assertEquals("There were the wrong number of lines returned", lines.length, data.size());

			//Ensure that the returned lines are the same as the file
			for (int i = 0; i < data.size(); i++)
			{
				assertEquals(lines[i], data.get(i).getData());
			}
		}
		//</editor-fold>
	}

	/**
	 * This method merges the byte arrays that exist in the packets in this list
	 * into a single packet
	 *
	 * @param packets the packets to merge
	 *
	 * @return the merged packets
	 */
	private byte[] bytesFromPackets(List<NioPacket> packets)
	{
		//Get the bytes out of the packets
		int size = 0;
		for (NioPacket packet : packets)
		{
			//Find out the total size we need for the resulting data
			byte[] data = (byte[]) packet.getData();
			size += data.length;
		}

		//Get a new ByteBuffer to hold it
		ByteBuffer total = ByteBuffer.wrap(new byte[size]);

		//Merge all our bytes into one data
		for (NioPacket packet : packets)
		{
			total.put((byte[]) packet.getData());
		}

		//Return the array
		return total.array();
	}
}
