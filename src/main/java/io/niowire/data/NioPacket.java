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
	private final Object data;
	/**
	 * If this packet is treated as a raw packet (raw bytes are used in
	 * serialization)
	 */
	private final boolean raw;
	/**
	 * The raw data (or null if there isn't any)
	 */
	private byte[] rawData;

	/**
	 * This constructs a new packet which can have raw data (and optionally use
	 * it)
	 *
	 * @param uid     the unique identifier for the NioConnection which made
	 *                   this object
	 * @param data    the serialized object
	 * @param raw     if the packet should have its raw data used instead of its
	 *                   object in serialization
	 * @param rawData the raw data bytes in this packet
	 */
	public NioPacket(String uid, Object data, boolean raw, byte[] rawData)
	{
		this.data = data;
		this.source = uid;
		this.raw = raw;
		this.rawData = rawData;
	}

	/**
	 * Construct a new NioPacket containing this data from the source (the UID
	 * of the connection)
	 *
	 * @param uid  the unique identifier for the NioConnection which made this
	 *                object
	 * @param data the Object which this packet contains
	 */
	public NioPacket(String uid, Object data)
	{
		this.data = data;
		this.source = uid;
		this.raw = false;
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

	/**
	 * Gets if this packet should be treated as raw data
	 *
	 * @return if this packet should be treated as raw data
	 */
	public boolean isRaw()
	{
		return raw;
	}

	/**
	 * The raw data attached to this packet (or null if there is no raw data)
	 *
	 * @return the raw data attached to this packet (or null if there is none)
	 */
	public byte[] getRawData()
	{
		return rawData;
	}

	/**
	 * Generates a HashCode for this object based on the data contained in it.
	 *
	 * @return a hash code for this object
	 */
	@Override
	public int hashCode()
	{
		//Build our hashcode
		int hash = 7;
		hash = 97 * hash + (int) (this.timestamp ^ (this.timestamp >>> 32));
		hash = 97 * hash + (this.source != null ? this.source.hashCode() : 0);
		hash = 97 * hash + (this.data != null ? this.data.hashCode() : 0);

		//Return our hashcode
		return hash;
	}

	/**
	 * Checks for equality with the passed object
	 *
	 * @param obj the object to check
	 *
	 * @return true if the other object is equal to this object, false
	 *            otherwise.
	 */
	@Override
	public boolean equals(Object obj)
	{
		//Check fo nulls and classes
		if (obj == null)
		{
			return false;
		}
		if (getClass() != obj.getClass())
		{
			return false;
		}
		//Cast our object
		final NioPacket other = (NioPacket) obj;

		//Check the member variables
		if (this.timestamp != other.timestamp)
		{
			return false;
		}
		if ((this.source == null) ? (other.source != null) : !this.source.equals(other.source))
		{
			return false;
		}
		if (this.data != other.data && (this.data == null || !this.data.equals(other.data)))
		{
			return false;
		}

		//return true if we haven't already returned false
		return true;
	}

	/**
	 * Returns a String representation of this object
	 *
	 * @return a string representation of this object
	 */
	@Override
	public String toString()
	{
		//Build our string
		return "NioPacket{" + "timestamp=" + timestamp + ", source=" + source + ", data=" + data + '}';
	}
}
