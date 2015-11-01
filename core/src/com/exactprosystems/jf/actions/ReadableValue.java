////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions;

public class ReadableValue
{
	public static ReadableValue TRUE = new ReadableValue("true");
	public static ReadableValue FALSE = new ReadableValue("false");

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
	
	private String value;
	private String description;
}
