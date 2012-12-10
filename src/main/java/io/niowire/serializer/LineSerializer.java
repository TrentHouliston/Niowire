/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.serializer;

import io.niowire.data.ObjectPacket;
import io.niowire.server.NioConnection.Context;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 *
 * @author trent
 */
public class LineSerializer extends DelimitedSerializer
{

	Charset CHARSET;
	CharsetDecoder DECODER;
	CharsetEncoder ENCODER;

	@Override
	protected List<ObjectPacket> deserializeBlob(ByteBuffer blob) throws CharacterCodingException
	{
		String str = DECODER.decode(blob).toString();

		Object data = deserializeString(str);

		return Collections.singletonList(new ObjectPacket(context.getUid(), data));
	}

	@Override
	protected ByteBuffer serializeBlob(List<ObjectPacket> objects)
	{
		return null;
	}

	@Override
	public void configure(Map<String, Object> configuration) throws Exception
	{
		String charset = (String) configuration.get("charset");
		CHARSET = Charset.forName(charset);
		DECODER = CHARSET.newDecoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
		ENCODER = CHARSET.newEncoder().onMalformedInput(CodingErrorAction.IGNORE).onUnmappableCharacter(CodingErrorAction.IGNORE);
	}

	@Override
	protected byte[] getDelimiter()
	{
		return "\n".getBytes(CHARSET);
	}

	protected Object deserializeString(String str)
	{
		return str;
	}

	protected String serializeString(Object obj)
	{
		return obj.toString();
	}
}
