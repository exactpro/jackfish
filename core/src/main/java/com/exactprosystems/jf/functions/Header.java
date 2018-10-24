/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.functions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

/**
 * A header for the {@link Table}
 */
class Header
{
	String name;
	Header.HeaderType type;
	public int index;

	public enum HeaderType
	{
		STRING		(String.class),
		BOOL		(Boolean.class),
		INT			(Integer.class),
		DOUBLE		(Double.class),
		DATE		(Date.class),
		BIG_DECIMAL	(BigDecimal.class),
		EXPRESSION	(Object.class),
		GROUP		(String.class),
		HYPERLINK	(String.class),
		COLORED		(String.class),
		;
		Class<?> clazz;

		HeaderType(Class<?> clazz)
		{
			this.clazz = clazz;
		}
		
		public static Header.HeaderType forName(String columnClassName)
		{
			return Arrays.stream(values())
					.filter(item -> item.clazz.getSimpleName().equals(columnClassName))
					.findFirst()
					.orElse(null);
		}

		public int compare(Object obj1, Object obj2, boolean ignoreCase)
		{
			if (!obj1.getClass().isAssignableFrom(this.clazz) && !obj2.getClass().isAssignableFrom(this.clazz))
			{
				return ignoreCase ? String.valueOf(obj1).compareToIgnoreCase(String.valueOf(obj2)) : String.valueOf(obj1).compareTo(String.valueOf(obj2));
			}
			if (Comparable.class.isAssignableFrom(this.clazz))
			{
				if (obj1 instanceof String && obj2 instanceof String)
				{
					return ignoreCase ? String.valueOf(obj1).compareToIgnoreCase(String.valueOf(obj2)) : String.valueOf(obj1).compareTo(String.valueOf(obj2));
				}
				else
				{
					try
					{
						Method compareTo = this.clazz.getMethod("compareTo", Object.class);
						return ((int) compareTo.invoke(obj1, obj2));
					}
					catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e)
					{}
					return 0;
				}
			}
			else
			{
				return String.valueOf(obj1).compareTo(String.valueOf(obj2));
			}
		}
	}

	public Header(String name, Header.HeaderType type)
	{
		this.name = name;
		this.type = type;
		this.index = this.getIndex();
	}

	/**
	 * copy constructor
	 */
	public Header(Header header)
	{
		this.name = header.name;
		this.type = header.type;
		this.index = this.getIndex();
	}

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
		if (o == null || this.getClass() != o.getClass())
			return false;

		Header header = (Header) o;
		return Objects.equals(this.index, header.index) && Objects.equals(this.name, header.name);
	}

	@Override
	public int hashCode()
	{
		return index;
	}

	//region private methods
	private int getIndex()
	{
		return Table.index++;
	}
	//endregion
}