/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package io.niowire.serializer;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.util.LinkedHashMap;

/**
 *
 * @author trent
 */
public class JsonSerializer extends LineSerializer
{

	Gson g = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

	@Override
	protected Object deserializeString(String str)
	{
		return g.fromJson(str, LinkedHashMap.class);
	}
}
