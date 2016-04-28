////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlValue;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.api.common.Str;

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

	@XmlValue
	protected String str = null;
	
	@XmlTransient
	protected boolean changed = false;
}
