////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.Str;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = "Returns rows which contains the given string")
public class StringCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 2230671827371752791L;

	public StringCondition(String name, String value)
	{
		this(name, value, false);
	}

	public StringCondition(String name, String value, boolean ignoreCase)
	{
		super(name);
		this.value = value;
		this.ignoreCase = ignoreCase;
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + separator + this.value + separator + this.ignoreCase + finish;
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		String name = getName();
		if (Str.IsNullOrEmpty(name))
		{
			return true;
		}
		Object value = map.get(name);
		String strValue = "" + value;
		if (this.ignoreCase)
		{
			return strValue.equalsIgnoreCase(this.value);
		}
		return strValue.equalsIgnoreCase(this.value);
	}

	@Override
	public String toString()
	{
		return StringCondition.class.getSimpleName() + " [name=" + getName() + ", value=" + value + ", ignoreCase=" + ignoreCase + "]";
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		if (this.ignoreCase)
		{
			return "'" + String.valueOf(this.value).toLowerCase() + "' != '" + String.valueOf(actualValue).toLowerCase() + "'";
		}
		
		return "'" + this.value + "' != '" + actualValue + "'";
	}

	public String getValue()
	{
		return value;
	}

	private boolean ignoreCase;
	
	private String value = null;

}
