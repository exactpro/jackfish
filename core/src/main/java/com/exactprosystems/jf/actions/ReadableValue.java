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

package com.exactprosystems.jf.actions;

public class ReadableValue
{
	private final String value;
	private final String description;

	public static final ReadableValue TRUE  = new ReadableValue("true");
	public static final ReadableValue FALSE = new ReadableValue("false");

	public ReadableValue(String value, String description)
	{
		this.value = value;
		this.description = description;
	}

	public ReadableValue(String value)
	{
		this(value, null);
	}

	@Override
	public int hashCode()
	{
		return ((value == null) ? 0 : value.hashCode());
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ReadableValue other = (ReadableValue) obj;
		if (value == null)
		{
			if (other.value != null)
				return false;
		}
		else if (!value.equals(other.value))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return this.value + (this.description == null ? "" : " <" + this.description + ">");
	}
	
	public String getValue()
	{
		return this.value;
	}
}
