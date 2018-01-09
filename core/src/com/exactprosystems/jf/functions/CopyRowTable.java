////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.common.WrongExpressionException;

import java.util.*;
import java.util.stream.Collectors;


public class CopyRowTable extends RowTable implements Cloneable
{
	private Map<String, Object> source;

	public CopyRowTable()
	{
		super(null, 0);
		this.source = new LinkedHashMap<>();
	}

	public CopyRowTable(Map<String, Object> map)
	{
		this();
		Objects.requireNonNull(map, "map");
		this.source.putAll(map);
	}
	
	public void makeStrValues(Set<String> names)
	{
		this.source = this.source.entrySet()
				.stream()
				.filter(e -> names.contains(e.getKey()))
				.collect(Collectors.toMap(Entry::getKey, v -> Str.asString(v.getValue()), (k, v) -> k, LinkedHashMap::new));
	}

	@Override
	public String toString()
	{
		return this.source.toString();
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.source);
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		if (obj == null)
		{
			return false;
		}
		if (this.getClass() != obj.getClass())
		{
			return false;
		}
		CopyRowTable other = (CopyRowTable) obj;

		Map<String, Object> otherSource = other.source;
		if (this.source == otherSource)
		{
			return true;
		}

		if (this.source.size() != otherSource.size())
		{
			return false;
		}

		Iterator<Entry<String, Object>> thisIterator = this.entrySet().iterator();
		try
		{
			while (thisIterator.hasNext())
			{
				Entry<String, Object> next = thisIterator.next();
				String key = next.getKey();
				Object thisValue = next.getValue();

				if (!otherSource.containsKey(key))
				{
					return false;
				}

				if (thisValue == null)
				{
					if (otherSource.get(key) != null)
					{
						return false;
					}
				}
				else
				{
					Object otherValue = otherSource.get(key);
					if (thisValue.getClass().isArray() && otherValue.getClass().isArray())
					{
						//Create object[] and place there thisValue and otherValue to compare arrays of any type
						Object[] arr1 = {thisValue};
						Object[] arr2 = {otherValue};
						boolean equals = Arrays.deepEquals(arr1, arr2);
						if (!equals)
						{
							return false;
						}
					}
					else
					{
						if (!Objects.equals(thisValue, otherValue))
						{
							return false;
						}
					}
				}
			}
		}
		catch (Exception e)
		{
			return false;
		}
		return true;
	}

	//region Interface Map
	@Override
	public int size()
	{
		return this.source.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.source.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.source.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.source.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
		Object value = this.source.get(key);
		if (value instanceof Exception)
		{
			Exception e = (Exception) value;
			throw new WrongExpressionException(e.getMessage());
		}
		return value;
	}

	@Override
	public Object put(String key, Object value)
	{
		return this.source.put(key, value);
	}

	@Override
	public Object remove(Object key)
	{
		return this.source.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.source.putAll(m);
	}

	@Override
	public void clear()
	{
		this.source.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.source.keySet();
	}

	@Override
	public Collection<Object> values()
	{
		return this.source.values();
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet()
	{
		return this.source.entrySet();
	}
	//endregion
}
