////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import com.exactprosystems.jf.api.app.Mutable;

@XmlAccessorType(XmlAccessType.NONE)
public class Parameter implements Mutable
{
	@XmlElement(name = Configuration.parametersKey)
	protected String key;
	
	@XmlElement(name = Configuration.parametersValue)
	protected String value;

    @Override
    public String toString()
    {
        return Parameter.class.getSimpleName() + "{" + Configuration.parametersKey + "=" + key + " " + Configuration.parametersValue + "=" + value + "}";
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

	@Override
	public boolean equals(Object o)
	{
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		Parameter parameter = (Parameter) o;

		if (key != null ? !key.equals(parameter.key) : parameter.key != null)
			return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return key != null ? key.hashCode() : 0;
	}

	private boolean changed = false;

	public Parameter setKey(String key)
	{
		this.key = key;
		return this;
	}

	public Parameter setValue(String value)
	{
		this.value = value;
		return this;
	}

	public String getKey()
	{
		return this.key;
	}

	public String getValue()
	{
		return this.value;
	}

}