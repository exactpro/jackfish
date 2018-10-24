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

package com.exactprosystems.jf.tool.custom.helper;

public class SimpleVariable
{
	private final String name;
	private final Object value;
	private final Class<?> clazz;

	public SimpleVariable(String name, Object value)
	{
		this.name = name;
		this.value = value;
		if (value != null)
		{
			this.clazz = value.getClass();
		}
		else
		{
			this.clazz = null;
		}
	}

	public String getName()
	{
		return name;
	}

	public Object getValue()
	{
		return value;
	}

	public Class<?> getClazz()
	{
		return clazz;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || getClass() != o.getClass())
		{
			return false;
		}

		SimpleVariable that = (SimpleVariable) o;

		if (name != null ? !name.equals(that.name) : that.name != null)
		{
			return false;
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		return name != null ? name.hashCode() : 0;
	}
}
