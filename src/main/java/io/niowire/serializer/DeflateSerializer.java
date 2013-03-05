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
import io.niowire.entities.Initialize;
import io.niowire.entities.NioObjectCreationException;
import io.niowire.entities.NioObjectFactory;
import io.niowire.server.NioConnection;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.*;
import javax.inject.Inject;
import javax.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static io.niowire.serializer.DelimitedSerializer.transferMax;

/**
 * This class uses the Deflate algorithm to compress any data which is leaving
 * it and decompress any data coming into it using an internal serializer.
 *
 * @author Trent Houliston
 */
public class DeflateSerializer implements NioSerializer
{

	private static final Logger LOG = LoggerFactory.getLogger(DeflateSerializer.class);
	//Injected Variables
	@Inject
	@Named("serializer")
	protected NioObjectFactory<NioSerializer> factory;
	@Inject
	protected NioConnection.Context context;
	@Inject
	protected int compressionLevel = Deflater.DEFAULT_COMPRESSION;
	@Inject
	protected int bufferSize = 2048;
	//
	protected NioSerializer serializer;
	protected Inflater inflater = null;
	protected Deflater deflater = null;
	protected ByteBuffer rebuffer = null;
	private ByteBuffer compressed = null;
	private ByteBuffer uncompressed = null;
	private ByteBuffer work = null;

	/**
	 * Builds our internal serializer as well as our inflater/deflater
	 *
	 * @throws NioObjectCreationException
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@Initialize
	protected void setup() throws NioObjectCreationException, ClassNotFoundException, IOException
	{
		//Build the internal serializer
		this.serializer = factory.create(Collections.singletonMap("context", context));

		//Build our inflater and deflater
		this.inflater = new Inflater(true);
		this.deflater = new Deflater(compressionLevel, true);

		//Allocate our input and output buffers also flip them so they are readable as empty
		this.compressed = ByteBuffer.allocate(bufferSize);
		this.compressed.flip();
		this.uncompressed = ByteBuffer.allocate(bufferSize);
		this.uncompressed.flip();
		this.work = ByteBuffer.allocate(bufferSize);
		this.work.flip();
	}

	/**
	 * Decompress the passed buffer before sending the data onward to the
	 * internal serializer.
	 *
	 * @param buffer the buffer to decompress
	 *
	 * @return the packets which were returned by the internal serializer
	 *
	 * @throws IOException
	 */
	@Override
	public List<NioPacket> deserialize(ByteBuffer buffer) throws IOException
	{
		try
		{
			//Get the data
			byte[] input = new byte[buffer.remaining()];
			//Get somewhere to put the decompressed data
			byte[] output = new byte[2048];
			buffer.get(input);

			//Inflate it
			inflater.setInput(input);
			LinkedList<NioPacket> packets = new LinkedList<NioPacket>();

			//Inflate all our data and send it to our internal serializer
			while (!inflater.needsInput() && !inflater.finished())
			{
				int inflated = inflater.inflate(output);
				ByteBuffer decompressed = ByteBuffer.wrap(output, 0, inflated);
				packets.addAll(serializer.deserialize(decompressed));
			}

			return packets;
		}
		catch (DataFormatException ex)
		{
			throw new IOException(ex);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void serialize(NioPacket packet) throws IOException
	{
		serializer.serialize(packet);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int read(ByteBuffer buffer) throws IOException
	{
		//Our start position
		int pos = buffer.position();

		//Put in our rebuffer data if we have any
		if (rebuffer != null)
		{
			buffer.put(rebuffer);
			rebuffer = null;
		}

		//While we can write more data, and there is more data to write
		while (buffer.hasRemaining() && this.hasData())
		{
			//If we have compressed data then write it out
			if (compressed.hasRemaining())
			{
				transferMax(compressed, buffer);
				compressed.compact().flip();
			}
			//If our deflater does not need input
			else if (!deflater.needsInput())
			{
				//Get the array we are writing out too and the offset and length we are interested in
				byte[] output = compressed.array();
				int offset = compressed.limit();
				int length = compressed.capacity() - offset;

				//Compress and write our data out now
				int bytes = deflater.deflate(output, offset, length, Deflater.SYNC_FLUSH);

				//Advance our limit based on how many bytes we just wrote
				compressed.limit(offset + bytes);
			}
			//Otherwise we need to get more data from our internal serializer
			else
			{
				//Reset our buffer
				uncompressed.clear();

				//Read from our internal serializer and put it in the deflater
				serializer.read(uncompressed);
				uncompressed.flip();
				deflater.setInput(uncompressed.array(), 0, uncompressed.remaining());
			}
		}

		//Return the number of bytes we put in
		return buffer.position() - pos;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasData() throws IOException
	{
		return compressed.hasRemaining() || serializer.hasData() || (!deflater.needsInput() && !deflater.finished());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void rebuffer(ByteBuffer data) throws IOException
	{
		rebuffer = ByteBuffer.allocate(data.remaining());
		rebuffer.put(data);
		rebuffer.flip();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException
	{
		serializer.close();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isOpen()
	{
		return serializer.isOpen();
	}
}