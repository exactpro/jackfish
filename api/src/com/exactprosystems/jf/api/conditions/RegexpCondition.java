////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.io.Serializable;
import java.util.regex.Pattern;

public class RegexpCondition extends Condition implements Serializable
{
	private static final long serialVersionUID = -1292265002640952551L;

	private String pattern;

	public RegexpCondition(String name, String pattern)
	{
		super(name);
		this.pattern = pattern;
	}

	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		if (!isMatchedName(otherName))
		{
			return true;
		}
		String otherStrValue = "" + otherValue;
		return Pattern.compile(this.pattern).matcher(otherStrValue).find();
	}

	@Override
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
	{
		return false;
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "'" + String.valueOf(actualValue) + "' not suitable for regular expression '" + String.valueOf(this.pattern) + "'";
	}

	@Override
	public String toString()
	{
		return RegexpCondition.class.getSimpleName() + "[name="+getName()+", pattern=" +this.pattern+"]";
	}
}
