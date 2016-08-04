package com.exactprosystems.jf.functions;

import java.math.BigDecimal;
import java.util.Date;

class Header implements Cloneable
{
	public enum HeaderType
	{
		STRING		(String.class),
		BOOL		(Boolean.class),
		INT			(Integer.class),	
		DOUBLE		(Double.class),
		DATE		(Date.class),
		BIG_DECIMAL	(BigDecimal.class),
		EXPRESSION	(Object.class), 
		;
		
		HeaderType(Class<?> clazz)
		{
			this.clazz = clazz;
		}
		
		public static Header.HeaderType forName(String columnClassName)
		{
			for (Header.HeaderType item : values())
			{
				if (item.clazz.getSimpleName().equals(columnClassName))
				{
					return item;
				}
			}
			return null;
		}

		public Class<?> clazz;
	}
	public Header(String name, Header.HeaderType type)
	{
		this.name = name;
		this.type = type;
		this.index = getIndex();
	}

	public String name;
	
	@Override
	protected Object clone() throws CloneNotSupportedException
	{
		Header clone = (Header)super.clone();
		
		clone.name = this.name;
		clone.type = this.type;
		clone.index = getIndex();
		
		return clone;
	}

	Header.HeaderType type;

	public int index;

	@Override
	public String toString()
	{
		return this.name;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Header header = (Header) o;

		return index == header.index;

	}

	@Override
	public int hashCode()
	{
		return index;
	}

	private int getIndex()
	{
		return Table.index++;
	}
}