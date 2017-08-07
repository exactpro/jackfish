////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.common;

import java.io.Serializable;
import java.util.Objects;

public class SerializablePair<K extends Serializable, V extends Serializable> implements Serializable
{
	private static final long serialVersionUID = 9114576260026734146L;

	private K key;
	private V value;

	public K getKey()
	{
		return this.key;
	}

	public V getValue()
	{
		return this.value;
	}

	public SerializablePair(K key, V value)
	{
		this.key = Objects.requireNonNull(key);
		this.value = value;
	}

	@Override
	public String toString()
	{
		return this.key + "=" + this.value;
	}

	@Override
	public int hashCode()
	{
		return this.key.hashCode() * 13 + (this.value == null ? 0 : this.value.hashCode());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o instanceof SerializablePair)
		{
			SerializablePair pair = (SerializablePair) o;
			if (this.key != null ? !this.key.equals(pair.key) : pair.key != null)
			{
				return false;
			}
			if (this.value != null ? !this.value.equals(pair.value) : pair.value != null)
			{
				return false;
			}
			return true;
		}
		return false;
	}
}
