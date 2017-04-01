package com.exactprosystems.jf.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
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
		GROUP       (String.class),
        HYPERLINK   (String.class),
        COLORED     (String.class),
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

		public int compare(Object obj1, Object obj2)
		{
			if (!obj1.getClass().isAssignableFrom(this.clazz) && !obj2.getClass().isAssignableFrom(this.clazz))
			{
				return String.valueOf(obj1).compareTo(String.valueOf(obj2));
			}
			if (Comparable.class.isAssignableFrom(this.clazz))
			{
				try
				{
					Method compareTo = this.clazz.getMethod("compareTo", Object.class);
					return ((int) compareTo.invoke(obj1, obj2));
				}
				catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
				{
					e.printStackTrace();
				}
				return 0;
			}
			else
			{
				return String.valueOf(obj1).compareTo(String.valueOf(obj2));
			}
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