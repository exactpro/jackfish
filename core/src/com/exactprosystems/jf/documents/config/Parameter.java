////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.config;

import com.exactprosystems.jf.api.app.Mutable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

@XmlAccessorType(XmlAccessType.NONE)
public class Parameter implements Mutable
{
	@XmlElement(name = Configuration.parametersKey)
	protected String key;
	
	@XmlElement(name = Configuration.parametersValue)
	protected String value;

	private boolean changed = false;

	//region interface mutable
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
	//endregion

	public Parameter setKey(String key)
	{
		this.changed = true;
		this.key = key;
		return this;
	}

	public Parameter setValue(String value)
	{
		this.changed = true;
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

	@Override
	public String toString()
	{
		return Parameter.class.getSimpleName() + "{" + Configuration.parametersKey + "=" + key + " " + Configuration.parametersValue + "=" + value + "}";
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


}