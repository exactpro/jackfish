/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.client;

import java.nio.ByteBuffer;
import java.util.Arrays;

public class FlexBuffer
{
	private enum Bytes 
	{ InByte(1), InChar(2), InInteger(4), InShort(2),  InLong(8), InDouble(8), InFloat(4); 
		
		Bytes(int number)
		{
			this.number = number;
		}
	
		public int number;
	}
	
	
	public FlexBuffer(int startCapacity)
	{
		this.buffer = ByteBuffer.allocate(startCapacity);
		
	}
	
	public ByteBuffer buffer()
	{
		return this.buffer;
	}
	
	public int position()
	{
		return this.buffer.position();
	}
	
	public FlexBuffer position(int newPosition)
	{
		expandIfNeed(0, newPosition);
		this.buffer.position(newPosition);
		return this;
	}

	public FlexBuffer trim()
	{
		if ((this.buffer.position()) < this.buffer.capacity())
		{
			byte[] newArray = Arrays.copyOf(this.buffer.array(), this.buffer.position());
			this.buffer = ByteBuffer.wrap(newArray);
			this.buffer.position(0);
		}

		return this;
	}

	public FlexBuffer put(byte b)
	{
		expandIfNeed(Bytes.InByte.number);
		this.buffer.put(b);
		return this;
	}
	
	public FlexBuffer put(int index, byte b)
	{
		expandIfNeed(Bytes.InByte.number, index);
		this.buffer.put(index, b);
		return this;
	}

	public FlexBuffer put(byte[] src)
	{
		expandIfNeed(src.length);
		this.buffer.put(src);
		return this;
	}
	
	public FlexBuffer putChar(char ch)
	{
		expandIfNeed(Bytes.InChar.number);
		this.buffer.putChar(ch);
		return this;
	}

	public FlexBuffer putChar(int index, char ch)
	{
		expandIfNeed(Bytes.InChar.number, index);
		this.buffer.putChar(index, ch);
		return this;
	}
	
	public FlexBuffer putDouble(double value)
	{
		expandIfNeed(Bytes.InDouble.number);
		this.buffer.putDouble(value);
		return this;
	}

	public FlexBuffer putDouble(int index, double value)
	{
		expandIfNeed(Bytes.InDouble.number, index);
		this.buffer.putDouble(index, value);
		return this;
	}
	
	public FlexBuffer putFloat(float value)
	{
		expandIfNeed(Bytes.InFloat.number);
		this.buffer.putFloat(value);
		return this;
	}

	public FlexBuffer putFloat(int index, float value)
	{
		expandIfNeed(Bytes.InFloat.number, index);
		this.buffer.putFloat(index, value);
		return this;
	}
	
	public FlexBuffer putInt(int value)
	{
		expandIfNeed(Bytes.InInteger.number);
		this.buffer.putInt(value);
		return this;
	}

	public FlexBuffer putInt(int index, int value)
	{
		expandIfNeed(Bytes.InInteger.number, index);
		this.buffer.putInt(index, value);
		return this;
	}
	
	public FlexBuffer putLong(long value)
	{
		expandIfNeed(Bytes.InLong.number);
		this.buffer.putLong(value);
		return this;
	}

	public FlexBuffer putLong(int index, long value)
	{
		expandIfNeed(Bytes.InLong.number, index);
		this.buffer.putLong(index, value);
		return this;
	}
	
	public FlexBuffer putShort(short value)
	{
		expandIfNeed(Bytes.InShort.number);
		this.buffer.putShort(value);
		return this;
	}

	public FlexBuffer putShort(int index, short value)
	{
		expandIfNeed(Bytes.InShort.number, index);
		this.buffer.putShort(index, value);
		return this;
	}
	
	public FlexBuffer putString(String str)
	{
		expandIfNeed(str.length());
		this.buffer.put(str.getBytes());
		return this;
	}

	public FlexBuffer putString(int index, String str)
	{
		expandIfNeed(str.length(), index);
		this.buffer.put(str.getBytes(), index, str.length());
		return this;
	}
	
	public byte get()
	{
		return this.buffer.get();
	}
	
	public byte get(int index)
	{
		return this.buffer.get(index);
	}
	
	public char getChar()
	{
		return this.buffer.getChar();
	}

	public char getChar(int index)
	{
		return this.buffer.getChar(index);
	}
	
	public double getDouble()
	{
		return this.buffer.getDouble();
	}

	public double getDouble(int index)
	{
		return this.buffer.getDouble(index);
	}
	
	public float getFloat()
	{
		return this.buffer.getFloat();
	}

	public float getFloat(int index)
	{
		return this.buffer.getFloat(index);
	}
	
	public int getInt()
	{
		return this.buffer.getInt();
	}

	public int getInt(int index)
	{
		return this.buffer.getInt(index);
	}

	public long getLong()
	{
		return this.buffer.getLong();
	}

	public long getLong(int index)
	{
		return this.buffer.getLong(index);
	}

	public short getShort()
	{
		return this.buffer.getShort();
	}

	public short getShort(int index)
	{
		return this.buffer.getShort(index);
	}
	
	public String getString(int len)
	{
		byte[] str = Arrays.copyOfRange(this.buffer.array(), this.buffer.position(), this.buffer.position() + len);
		return new String(str);
	}

	public String getString(int index, int len)
	{
		byte[] str = Arrays.copyOfRange(this.buffer.array(), index, index + len);
		return new String(str);
	}
	
	
	@Override
	public String toString()
	{
		return FlexBuffer.class.getSimpleName() + " [ cap=" + this.buffer.capacity() + " pos=" + this.buffer.position() + "]";
	}
	
	private void expandIfNeed(int sizeToPut, int index)
	{
		if (this.buffer.capacity() < index + sizeToPut)
		{
			int newSize = Math.max(this.buffer.capacity() * 2 + 1, index + sizeToPut + 1);
			byte[] newArray = Arrays.copyOf(this.buffer.array(), newSize);
			int pos = this.buffer.position();
			this.buffer = ByteBuffer.wrap(newArray);
			this.buffer.position(pos);
		}
	}

	private void expandIfNeed(int sizeToPut)
	{
		expandIfNeed(sizeToPut, this.buffer.position());
	}
	
	private ByteBuffer buffer;
}
