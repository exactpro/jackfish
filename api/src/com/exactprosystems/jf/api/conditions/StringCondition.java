////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.io.Serializable;

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
	public String toString()
	{
		return StringCondition.class.getSimpleName() + " [name=" + getName() + ", value=" + value + ", ignoreCase=" + ignoreCase + "]";
	}

	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		if (!isMatchedName(otherName))
		{
			return true;
		}

		String otherStrValue = "" + otherValue;
		
		if (this.ignoreCase)
		{
			return this.value.equalsIgnoreCase(otherStrValue);
		}
			
		return this.value.equals(otherStrValue);
	}

	@Override
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
	{
		return false;
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

	//TODO these methods need to WinPlugin
	public boolean isIgnoreCase()
	{
		return ignoreCase;
	}

	public String getValue()
	{
		return value;
	}

	private boolean ignoreCase;
	
	private String value = null;

}
