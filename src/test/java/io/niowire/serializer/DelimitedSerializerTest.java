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
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Unit tests for the {@link DelimitedSerializer}
 *
 * @author Trent Houliston
 */
public class DelimitedSerializerTest
{

	/**
	 * Tests that the deserializer is correctly delimiting and removing
	 * delimiters, as well as splitting up the data into it's component packets.
	 *
	 * @throws Exception
	 */
	@Test
	public void testDeserialize() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Create an input array with random characters
		byte[] inputArray = randomCharFill(new byte[128]);

		//Add 3 splits
		inputArray[22] = (byte) '\n';
		inputArray[67] = (byte) '\n';
		inputArray[127] = (byte) '\n';

		//Get the expected results
		byte[] expected1 = Arrays.copyOfRange(inputArray, 0, 22);
		byte[] expected2 = Arrays.copyOfRange(inputArray, 23, 67);
		byte[] expected3 = Arrays.copyOfRange(inputArray, 68, 127);

		//Wrap the input in a byte buffer as our input
		ByteBuffer input = ByteBuffer.wrap(inputArray);

		//Deserialize our input
		List<NioPacket> output = serializer.deserialize(input);

		//Check there are three splits
		assertEquals("There should be 3 packets returned", 3, output.size());

		//Check that each of the return arrays are the expected result
		assertArrayEquals("The first packet did not have the expected result", expected1, (byte[]) output.get(0).getData());
		assertArrayEquals("The second packet did not have the expected result", expected2, (byte[]) output.get(1).getData());
		assertArrayEquals("The third packet did not have the expected result", expected3, (byte[]) output.get(2).getData());
	}

	/**
	 * Tests that the serializer is caching unused data to be used in later
	 * packets.
	 *
	 * @throws Exception
	 */
	@Test
	public void testBuffering() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Create two new input arrays filled with random characters
		byte[] inputArray1 = randomCharFill(new byte[64]);
		byte[] inputArray2 = randomCharFill(new byte[64]);

		//Add a split in the first one, and two in the second
		inputArray1[35] = (byte) '\n';
		inputArray2[12] = (byte) '\n';
		inputArray2[63] = (byte) '\n';

		//Get the expected results
		byte[] expected1 = Arrays.copyOfRange(inputArray1, 0, 35);

		//Get the bits of the two buffers for our second expected result
		byte[] expected2Half1 = Arrays.copyOfRange(inputArray1, 36, 64);
		byte[] expected2Half2 = Arrays.copyOfRange(inputArray2, 0, 12);

		//Join them together
		byte[] expected2 = new byte[expected2Half1.length + expected2Half2.length];
		System.arraycopy(expected2Half1, 0, expected2, 0, expected2Half1.length);
		System.arraycopy(expected2Half2, 0, expected2, expected2Half1.length, expected2Half2.length);

		//Get our third expected result
		byte[] expected3 = Arrays.copyOfRange(inputArray2, 13, 63);

		//Wrap the two inputs in byte buffers as our input
		ByteBuffer input1 = ByteBuffer.wrap(inputArray1);
		ByteBuffer input2 = ByteBuffer.wrap(inputArray2);

		//Deserialize our first input
		List<NioPacket> output1 = serializer.deserialize(input1);

		//Check that only one result was returned
		assertEquals("There should be 1 packets returned", 1, output1.size());

		//Check our first packet has the expected result
		assertArrayEquals("The first packet did not have the expected result", expected1, (byte[]) output1.get(0).getData());

		//Deserialize our second input
		List<NioPacket> output2 = serializer.deserialize(input2);

		//Check there are three splits
		assertEquals("There should be 2 packets returned", 2, output2.size());

		//Check that each of the return arrays are the expected result for the second half
		assertArrayEquals("The second packet did not have the expected result", expected2, (byte[]) output2.get(0).getData());
		assertArrayEquals("The third packet did not have the expected result", expected3, (byte[]) output2.get(1).getData());
	}

	/**
	 * Tests that data entered by the serialize method is able to be read back
	 *
	 * @throws Exception
	 */
	@Test
	public void testRead() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Check that we do not currently have data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

		//Create some test data of random characters
		byte[] test = randomCharFill(new byte[1024]);
		NioPacket packet = new NioPacket("TEST", test);

		//Set our expected result
		byte[] expected = new byte[1025];
		System.arraycopy(test, 0, expected, 0, test.length);
		expected[1024] = (byte) '\n';

		//Serialize the packet
		serializer.serialize(packet);

		//(all our data plus one for the delimiter)
		ByteBuffer output = ByteBuffer.allocate(1025);

		//We should have hasData be true
		assertTrue("The serializer should have data at this point", serializer.hasData());

		//Read into the output
		serializer.read(output);
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The output did not have the expected amount of data", 1025, output.remaining());

		//Put the data we got into an array
		byte[] result = new byte[1025];
		output.get(result);
		assertFalse("All data should have been read from the buffer", output.hasRemaining());

		//Check that we got the expected result
		assertArrayEquals("The data that was read back was not the expected result", expected, result);

		//Check that we now have no data
		assertFalse("The serializer should not have data at this point", serializer.hasData());
	}

	/**
	 * Tests that for large packets being read out, the data is split up and
	 * returned in chunks
	 *
	 * @throws Exception
	 */
	@Test
	public void testSplitRead() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Check that we do not currently have data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

		//Create some test data
		byte[] test = randomCharFill(new byte[1024]);
		NioPacket packet = new NioPacket("TEST", test);

		//Set our expected result arrays
		byte[] expected1 = new byte[500];
		byte[] expected2 = new byte[500];
		byte[] expected3 = new byte[25];

		//Copy the data into position
		System.arraycopy(test, 0, expected1, 0, expected1.length);
		System.arraycopy(test, expected1.length, expected2, 0, expected2.length);
		System.arraycopy(test, expected1.length + expected2.length, expected3, 0, expected3.length - 1);
		expected3[24] = (byte) '\n';

		//Serialize the packet
		serializer.serialize(packet);

		//Only enough for 500 bytes of data (should take 3 reads)
		ByteBuffer output = ByteBuffer.allocate(500);

		//We should have hasData be true
		assertTrue("The serializer should have data at this point", serializer.hasData());

		//Read into the output
		output.clear();
		serializer.read(output);
		assertTrue("There should still be data remaining in the serializer", serializer.hasData());
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The first output did not have the expected amount of data", 500, output.remaining());
		//Put the data we got into an array
		byte[] result1 = new byte[500];
		output.get(result1);
		//Check that we got the expected result
		assertArrayEquals("The data that was read from the first read back was not the expected result", expected1, result1);

		//Read into the output
		output.clear();
		serializer.read(output);
		assertTrue("There should still be data remaining in the serializer", serializer.hasData());
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The second output did not have the expected amount of data", 500, output.remaining());
		//Put the data we got into an array
		byte[] result2 = new byte[500];
		output.get(result2);
		//Check that we got the expected result
		assertArrayEquals("The data that was read from the second read back was not the expected result", expected2, result2);

		//Read into the output
		output.clear();
		serializer.read(output);
		assertFalse("All data should have been read from the serializer", serializer.hasData());
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The third output did not have the expected amount of data", 25, output.remaining());
		//Put the data we got into an array
		byte[] result3 = new byte[25];
		output.get(result3);
		//Check that we got the expected result
		assertArrayEquals("The data that was read from the third read back was not the expected result", expected3, result3);

		//Check that we now have no data
		assertFalse("The serializer should not have data at this point", serializer.hasData());
	}

	/**
	 * Tests that multiple serialized packets will all get output into a single
	 * buffer.
	 *
	 * @throws Exception
	 */
	@Test
	public void testMultiPacketSerialize() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Check that we do not currently have data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

		byte[][] testBytes = new byte[5][];
		NioPacket[] packets = new NioPacket[5];
		byte[] expected = new byte[(500 * 5) + 5];

		for (int i = 0; i < testBytes.length; i++)
		{
			testBytes[i] = randomCharFill(new byte[500]);
			packets[i] = new NioPacket("TEST", testBytes[i]);
			expected[(i + 1) * 500 + i] = (byte) '\n';
			System.arraycopy(testBytes[i], 0, expected, (i * 500) + i, testBytes[i].length);
		}

		//Serialize the packets
		for (NioPacket packet : packets)
		{
			serializer.serialize(packet);
		}

		//(all our data plus one for the delimiter)
		ByteBuffer output = ByteBuffer.allocate((500 * 5) + 5);

		//We should have hasData be true
		assertTrue("The serializer should have data at this point", serializer.hasData());

		//Read into the output
		serializer.read(output);
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The output did not have the expected amount of data", (500 * 5) + 5, output.remaining());

		//Put the data we got into an array
		byte[] result = new byte[500 * 5 + 5];
		output.get(result);
		assertFalse("All data should have been read from the buffer", output.hasRemaining());

		//Check that we got the expected result
		assertArrayEquals("The data that was read back was not the expected result", expected, result);

		//Check that we now have no data
		assertFalse("The serializer should not have data at this point", serializer.hasData());
	}

	/**
	 * Tests for the case when the delimiter will make the packet larger then
	 * the buffer (not the data in the buffer itself)
	 *
	 * @throws Exception
	 */
	@Test
	public void testBoundaryRead() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Check that we do not currently have data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

		//Create some test data of random characters
		byte[] test = randomCharFill(new byte[500]);
		NioPacket packet = new NioPacket("TEST", test);

		//Set our expected result
		byte[] expected = new byte[500];
		System.arraycopy(test, 0, expected, 0, test.length);

		//Serialize the packet
		serializer.serialize(packet);

		//All our data with no room for the delimiter
		ByteBuffer output = ByteBuffer.allocate(500);

		//We should have hasData be true
		assertTrue("The serializer should have data at this point", serializer.hasData());

		//Read into the output
		serializer.read(output);
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The output did not have the expected amount of data", 500, output.remaining());

		//Put the data we got into an array
		byte[] result = new byte[500];
		output.get(result);
		assertFalse("All data should have been read from the buffer", output.hasRemaining());

		//Check that we got the expected result
		assertArrayEquals("The data that was read back was not the expected result", expected, result);

		//Check that we still have data (there should be a delimiter sent to us)
		assertTrue("The serializer should still have data (our delimiter)", serializer.hasData());

		output.clear();
		serializer.read(output);
		output.flip();
		assertEquals("There should only be one character (the delimiter)", 1, output.remaining());
		assertEquals("The last character should be a delimiter", (byte) '\n', output.get());
		assertFalse("The serializer should now have no more data", serializer.hasData());
	}

	/**
	 * Tests that unused data can be returned to the serializer to be
	 * re-buffered and that it handles delimiters properly in this case.
	 *
	 * @throws Exception
	 */
	@Test
	public void testDataReturn() throws Exception
	{
		//Create a serializer
		DelimitedSerializer serializer = new DelimitedSerializerImpl();

		//Check that we do not currently have data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

		//Create some test data of random characters
		byte[] test = randomCharFill(new byte[1000]);
		NioPacket packet = new NioPacket("TEST", test);

		//Set our expected result
		byte[] expected1 = new byte[500];
		byte[] expected2 = new byte[501];
		System.arraycopy(test, 0, expected1, 0, expected1.length);
		System.arraycopy(test, expected1.length, expected2, 0, expected2.length - 1);
		expected2[500] = (byte) '\n';

		//Serialize the packet
		serializer.serialize(packet);

		//(all our data plus one for the delimiter)
		ByteBuffer output = ByteBuffer.allocate(700);

		//We should have hasData be true
		assertTrue("The serializer should have data at this point", serializer.hasData());

		//Read into the output
		serializer.read(output);
		output.flip();

		//Check that we read the expected amount of data
		assertEquals("The output did not have the expected amount of data", 700, output.remaining());

		//Put the data we got into an array
		byte[] result1 = new byte[500];
		output.get(result1);
		assertTrue("There should be data remaining in the buffer", output.hasRemaining());

		//Check that we got the expected result
		assertArrayEquals("The data that was read back was not the expected result", expected1, result1);

		serializer.rebuffer(output);

		output.clear();
		serializer.read(output);
		output.flip();

		byte[] result2 = new byte[501];
		output.get(result2);

		//Check that we got the expected result
		assertArrayEquals("The data that was read back was not the expected result", expected2, result2);

		//Check that we now have no data
		assertFalse("The serializer should not have data at this point", serializer.hasData());

	}

	/**
	 * This method takes a byte array and fills it with random a-z characters
	 *
	 * @param inputArray the array to fill
	 *
	 * @return the array filled with random a-z characters
	 */
	private byte[] randomCharFill(byte[] inputArray)
	{
		//Get a new random number generator
		Random r = new Random();

		//Loop through the array
		for (int i = 0; i < inputArray.length; i++)
		{
			//Set the value to a random character between a-z
			inputArray[i] = (byte) (r.nextInt(26) + 'a');
		}

		//Return the array
		return inputArray;
	}

	/**
	 * This class is an implementation of the DelimitedSerializer which
	 * implements it's abstract methods with simple methods that simply either
	 * store or return the same bytes given to it.
	 */
	public class DelimitedSerializerImpl extends DelimitedSerializer
	{

		/**
		 * This method deserializes the data into a byte array and makes this
		 * the data.
		 *
		 * @param blob the byte buffer which contains the data
		 *
		 * @return a {@link NioPacket} object containing a byte array of the
		 *               data
		 *
		 * @throws IOException
		 */
		@Override
		public List<NioPacket> deserializeBlob(ByteBuffer blob) throws IOException
		{
			byte[] bytes = new byte[blob.remaining()];
			blob.get(bytes);

			return Collections.singletonList(new NioPacket("TEST", bytes));
		}

		/**
		 * This method serializes a NioPacket (which is required to contain a
		 * byte array) into a buffer containing that byte array.
		 *
		 * @param packet the packet to be serialized
		 *
		 * @return a ByteBuffer containing the byte array in the packet
		 *
		 * @throws IOException
		 */
		@Override
		public ByteBuffer serializeBlob(NioPacket packet) throws IOException
		{
			byte[] data = (byte[]) packet.getData();
			return ByteBuffer.wrap(data);
		}

		/**
		 * Return the bytes of a new line (we are delimiting on \n)
		 *
		 * @return the bytes for \n (byte 10)
		 */
		@Override
		public byte[] getDelimiter()
		{
			return "\n".getBytes();
		}

		/**
		 * We don't have any configuration to do
		 *
		 * @param configuration the configuration to use
		 *
		 * @throws Exception
		 */
		@Override
		public void configure(Map<String, Object> configuration) throws Exception
		{
			//Do nothing
		}
	}
}
