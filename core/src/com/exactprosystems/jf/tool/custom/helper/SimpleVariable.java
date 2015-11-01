////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.helper;

public class SimpleVariable
{
	private String name;
	private Object value;
	private String clazz;

	public SimpleVariable(String name, Object value)
	{
		this.name = name;
		this.value = value;
		if (value != null)
		{
			this.clazz = value.getClass().getSimpleName();
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

	public void setName(String name)
	{
		this.name = name;
	}

	public Object getValue()
	{
		return value;
	}

	public void setValue(Object value)
	{
		this.value = value;
	}

	public String getClazz()
	{
		return clazz;
	}

	public void setClazz(String clazz)
	{
		this.clazz = clazz;
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
