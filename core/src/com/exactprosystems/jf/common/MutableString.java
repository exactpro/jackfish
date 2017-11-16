////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;

import java.util.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

@XmlAccessorType(XmlAccessType.NONE)
public class MutableString implements Mutable
{
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
	public String toString()
	{
		return this.str == null ? "" : this.str;
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
		this.changed = changed || !Str.areEqual(this.str, str);
		this.str = str;
	}

	public void set(MutableString str)
	{
		set(str.get());
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		MutableString that = (MutableString) o;

		return str != null ? str.equals(that.str) : that.str == null;

	}

	public boolean isEmpty()
	{
	    return this.str == null || this.str.isEmpty();
	}
	
	public boolean equals(String s)
	{
		return this.get().equals(s);
	}

	@Override
	public int hashCode()
	{
		return  Objects.hashCode(str);
	}

	@XmlValue
	protected String str = null;
	
	@XmlTransient
	protected boolean changed = false;
}
