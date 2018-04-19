/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
