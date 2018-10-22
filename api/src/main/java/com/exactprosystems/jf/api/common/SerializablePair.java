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
