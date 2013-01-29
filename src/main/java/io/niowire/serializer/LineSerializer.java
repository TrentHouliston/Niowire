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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.*;
import java.util.Collections;
import java.util.List;
import javax.inject.Inject;

/**
 * This class serializes new line delimited binary blobs into strings, It then
 * calls deserializeString on the string so that this class can be overloaded by
 * other classes which use line based object serialization. It will also call
 * serializeString which can be used to convert data into a string form to be
 * sent to the client.
 *
 * @author Trent Houliston
 */
public class LineSerializer extends DelimitedSerializer
{

	/**
	 * Our charset that we are going to use
	 */
	@Inject
	public Charset charset = null;
	/**
	 * Our encoder (note not thread safe)
	 */
	private CharsetDecoder DECODER = null;
	/**
	 * Our decoder (note not thread safe)
	 */
	private CharsetEncoder ENCODER = null;

	/**
	 * This method overrides from the Delimited serializer and deserializes the
	 * passed blob into a string. It then passes this to the deserializeString
	 * method to be further parsed.
	 *
	 * @param blob the byte buffer blob which has a window where our line is
	 *
	 * @return A NioPacket containing our string
	 *
	 * @throws NioInvalidDataException if the encoding we are using does not
	 *                                    exist or it is thrown by an
	 *                                    implementing class
	 */
	@Override
	protected List<NioPacket> deserializeBlob(ByteBuffer blob) throws NioInvalidDataException
	{
		//Decode the string
		String str;

		try
		{
			str = DECODER.decode(blob).toString();
		}
		catch (CharacterCodingException ex)
		{
			throw new NioInvalidDataException(ex);
		}

		//Remove any carriage returns
		str = str.replaceAll("\r", "");

		//Deserialize this
		Object data = deserializeString(str);

		//If we are adding the raw data
		if (raw)
		{
			return Collections.singletonList(new NioPacket(context.getUid(), data, true, (str + new String(getDelimiter(), charset)).getBytes(charset)));
		}
		else
		{
			return Collections.singletonList(new NioPacket(context.getUid(), data));
		}
	}

	/**
	 * This method is used to serialize a string into a ByteBuffer by using our
	 * charset to convert it into its binary form
	 *
	 * @param packet the objects to be serialized
	 *
	 * @return a byte buffer containing our serialized objects
	 *
	 * @throws NioInvalidDataException if the data that was given is invalid
	 */
	@Override
	protected ByteBuffer serializeBlob(NioPacket packet) throws NioInvalidDataException
	{
		//If we have null raw data
		if (raw && packet.isRaw() && packet.getRawData() == null)
		{
			return ByteBuffer.allocate(0);
		}
		//If we have raw data
		else if (raw && packet.isRaw())
		{
			return ByteBuffer.wrap(packet.getRawData());
		}
		//Otherwise behave as normal
		else
		{
			try
			{
				return ENCODER.encode(CharBuffer.wrap(serializeString(packet)));
			}
			catch (CharacterCodingException ex)
			{
				throw new NioInvalidDataException(ex);
			}
		}
	}

	/**
	 * This method initializes the Encoder and Decoder objects from our injected
	 * character set
	 *
	 * @throws Exception there is an exception while setting up the encoders
	 */
	@Initialize
	public void init() throws Exception
	{
		//Get an encoder and decoder from our charset
		DECODER = charset.newDecoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
		ENCODER = charset.newEncoder().onMalformedInput(CodingErrorAction.REPLACE).onUnmappableCharacter(CodingErrorAction.REPLACE);
	}

	/**
	 * Gets the delimiter for the line serializer, in this case we are using \n
	 * rather then \r\n or String.format("%n") as we are not sure which they
	 * will be using. Therefor if we use \n we will get all windows and linux
	 * encoded data so long as we strip out all \r from the output.
	 *
	 * @return the bytes for a new line character in our charset
	 */
	@Override
	protected byte[] getDelimiter()
	{
		return "\n".getBytes(charset);
	}

	/**
	 * This method is meant to be overridden by subclasses which use a line
	 * based deserializeation method. The default implementation just returns
	 * the string as the object.
	 *
	 * @param str the string to deserialize
	 *
	 * @return the string passed in
	 *
	 * @throws NioInvalidDataException if the passed in data was invalid
	 */
	protected Object deserializeString(String str) throws NioInvalidDataException
	{
		return str;
	}

	/**
	 * This method is meant to be overridden by subclasses which use a line
	 * based serialization method. The default implementation just returns
	 * obj.toString()
	 *
	 * @param obj the object to serialize
	 *
	 * @return a string representation of the object
	 *
	 * @throws NioInvalidDataException if the passed in data was invalid
	 */
	protected String serializeString(NioPacket obj) throws NioInvalidDataException
	{
		return obj.getData().toString();
	}
}
