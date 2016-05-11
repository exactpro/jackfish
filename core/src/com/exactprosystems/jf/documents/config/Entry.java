////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.api.app.Mutable;
import com.exactprosystems.jf.common.parser.items.MutableArrayList;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import java.util.List;
import java.util.Optional;

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
			this.parameters.forEach(Parameter::saved);
		}
		changed = false;
	}

	public final String get(String name) throws Exception
	{
		switch (name)
		{
			case Configuration.entryName : return this.entryNameValue;
		}
		return this.getParameter(name).orElse(getDerived(name));
	}

	public final void set(String name, Object value) throws Exception
	{
		switch (name)
		{
			case Configuration.entryName : this.entryNameValue = "" +value; return;
		}
		Parameter o = new Parameter();
		o.setKey(name);
		int index;
		if ((index = this.getParameters().indexOf(o)) != -1)
		{
			this.getParameters().get(index).setValue("" + value);
			return;
		}
		setDerived(name, value);
	}

	protected abstract String getDerived(String name) throws Exception;
	
	protected abstract void setDerived(String name, Object value) throws Exception;

	public Optional<String> getParameter(String key)
	{
		return this.getParameters()
				.stream()
				.filter(p -> p.key != null && p.key.equals(key))
				.map(Parameter::getValue)
				.findFirst();
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