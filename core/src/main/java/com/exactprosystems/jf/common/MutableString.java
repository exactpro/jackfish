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

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;
import java.util.Objects;

@XmlAccessorType(XmlAccessType.NONE)
public class MutableString implements Mutable
{
	@XmlValue
	protected String str = null;

	@XmlTransient
	protected boolean changed = false;

	public MutableString()
	{
		this("");
	}

	public MutableString(String str)
	{
		this.str = str;
		this.changed = false;
	}

	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		this.changed = false;
	}

	public String get()
	{
		return this.str;
	}

	public void set(String str)
	{
		this.changed = this.changed || !Str.areEqual(this.str, str);
		this.str = str;
	}

	public void set(MutableString str)
	{
		this.set(str.str);
	}

	public boolean isEmpty()
	{
		return Str.IsNullOrEmpty(this.str);
	}

	public boolean equals(String s)
	{
		return this.str.equals(s);
	}

	@Override
	public String toString()
	{
		return this.str == null ? "" : this.str;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
		{
			return true;
		}
		if (o == null || this.getClass() != o.getClass())
		{
			return false;
		}

		MutableString that = (MutableString) o;
		return this.str != null ? this.str.equals(that.str) : that.str == null;
	}

	@Override
	public int hashCode()
	{
		return Objects.hashCode(this.str);
	}
}
