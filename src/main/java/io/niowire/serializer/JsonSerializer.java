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

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.niowire.data.NioPacket;
import java.util.LinkedHashMap;

/**
 * This serializer expects each line to be a JSON object, which it will
 * deserialize into a Map. We are using google Gson to serialize/deserialize
 * objects.
 *
 * @author Trent Houliston
 */
public class JsonSerializer extends LineSerializer
{

	//Our gson instance
	Gson g = new GsonBuilder().disableHtmlEscaping().serializeNulls().create();

	/**
	 * Deserializes the passed string into a LinkedHashMap object treating the
	 * input as a JSON object
	 *
	 * @param str the string to use as the JSON object
	 *
	 * @return a LinkedHashMap containing the mapping of the JSON Object
	 */
	@Override
	protected Object deserializeString(String str)
	{
		return g.fromJson(str, LinkedHashMap.class);
	}

	/**
	 * Serializes the past object into a JSON string format
	 *
	 * @param obj the java object which we will serialize into JSON
	 *
	 * @return a string representation of the passed object in JSON
	 */
	@Override
	protected String serializeString(NioPacket obj)
	{
		return g.toJson(obj.getData());
	}
}
