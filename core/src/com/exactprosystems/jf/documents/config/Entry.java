////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;

@XmlAccessorType(XmlAccessType.NONE)
public abstract class Entry implements Mutable
{
	@XmlElement(name = Configuration.entryName)
	protected String entryNameValue;

	@XmlElement(name = Configuration.parametersEntry)
	protected MutableArrayList<Parameter> parameters;

	@Override
	public String toString()
	{
		return this.entryNameValue == null ? "" : this.entryNameValue;
	}

	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		if (this.parameters != null)
		{
			if (this.parameters.isChanged())
			{
				return true;
			}
			for (Parameter parameter : this.parameters)
			{
				if (parameter.isChanged())
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void saved()
	{
		if (this.parameters != null)
		{
			this.parameters.saved();
			for (Parameter parameter : this.parameters)
			{
				parameter.saved();
			}
		}
		changed = false;
	}
	
	public abstract String get(String name) throws Exception;
//	{
//		Object res = Configuration.get(getClass(), this, name);
//		return res == null ? "" : res.toString();
//	}
	
	public abstract void set(String name, Object value) throws Exception;
//	{
//		Configuration.set(getClass(), this, name, value);
//		changed = true;
//	}

	public String getParameter(String key)
	{
		if (this.parameters != null)
		{
			for (Parameter param : this.parameters)
			{
				if (param.key.equals(key))
				{
					return param.value;
				}
			}
		}
		
		return null;
	}

	public List<Parameter> getParameters()
	{
		if (this.parameters == null)
		{
			this.parameters = new MutableArrayList<Parameter>();
		}
		return this.parameters;
	}
	
	private boolean changed = false;
}