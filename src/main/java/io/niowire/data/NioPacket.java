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
package io.niowire.data;

/**
 * This class represents a packet containing an object. They are used as the
 * internal communication objects between the various services. They are created
 * from the raw byte stream coming into the serializer/deserializer.
 *
 * @author Trent Houliston
 */
public class NioPacket implements Comparable<NioPacket>
{

	/**
	 * The time this packet was created
	 */
	private final long timestamp = System.currentTimeMillis();
	/**
	 * The ID of the source of this packet (the ID given by NIO connection)
	 */
	private final String source;
	/**
	 * The data contained in this packet
	 */
	private Object data;

	/**
	 * Construct a new NioPacket containing this data from the source (the
	 * UID of the connection)
	 *
	 * @param uid  the unique identifier for the NioConnection which made this
	 *                object
	 * @param data the Object which this packet contains
	 */
	public NioPacket(String uid, Object data)
	{
		this.data = data;
		this.source = uid;
	}

	/**
	 * Gets the UID of the NioConnection which created this packet
	 *
	 * @return the source of the packet
	 */
	public String getSource()
	{
		return source;
	}

	/**
	 * Gets the data object which is contained in this packet
	 *
	 * @return the data object contained in this packet
	 */
	public Object getData()
	{
		return data;
	}

	/**
	 * Sets the data object which is contained in this packet
	 *
	 * @param data the data to set
	 */
	public void setData(Object data)
	{
		this.data = data;
	}

	/**
	 * Gets the timestamp that this packet was created
	 *
	 * @return the timestamp this packet was created
	 */
	public long getTimestamp()
	{
		return timestamp;
	}

	/**
	 * Compares this packet to another packed based on their timestamps
	 *
	 * @param o the other NioPacket to compare to
	 *
	 * @return a negative integer, zero, or a positive integer as this object is
	 *            less than, equal to, or greater than the specified object.
	 *
	 * @throws NullPointerException if the specified object is null
	 * @throws ClassCastException   if the specified object's type prevents it
	 *                                 from being compared to this object.
	 */
	@Override
	public int compareTo(NioPacket o)
	{
		return Long.compare(this.getTimestamp(), o.getTimestamp());
	}
}
