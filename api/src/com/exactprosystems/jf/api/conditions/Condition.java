////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.client.ICondition;

import java.awt.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Map;
import java.util.Map.Entry;

public abstract class Condition implements ICondition, Serializable
{
	private static final long	serialVersionUID	= -7581472488041624617L;
	
	public static final char 	separator			= '\001';
	public static final char 	start				= '{';
	public static final char 	finish				= '}';

	public 	static Condition[] convertToCondition(Map<String, Object> expected) throws Exception
	{
		Condition[] conditions = new Condition[expected.size()];
		
		int i = 0;
		for (Entry<String, Object> entry : expected.entrySet())
		{
			String expectedKey = entry.getKey().toString();
			Object expectedValue = entry.getValue();
			Condition condition = Condition.convertToCondition(expectedKey, expectedValue);
			conditions[i++] = condition;
		}

		return conditions;
	}
	
	public 	static Condition convertToCondition(String name, Object expectedValue) throws Exception
	{
		if (expectedValue instanceof Condition)
		{
			return (Condition)expectedValue;
		}
		else if (expectedValue == null)
		{
			return new EmptyCondition(name);
		}
		else if (expectedValue instanceof Number)
		{
			return new NumberCondition(name, (Number)expectedValue);
		}
		else if (expectedValue instanceof Date)
		{
			return new DateCondition(name, (Date)expectedValue);
		}
		else if (expectedValue instanceof Color)
		{
			return new ColorCondition(name, (Color)expectedValue);
		}
		
		return new StringCondition(name, "" + expectedValue);
	}
	
	public static AndCondition and(Condition ... cond) throws Exception
	{
		return new AndCondition(cond);
	}
	
	public static OrCondition or(Condition ... cond) throws Exception
	{
		return new OrCondition(cond);
	}
	
	public static NotCondition not(Condition cond)
	{
		return new NotCondition(cond);
	}

	public Condition(String name)
	{
		this.name = name;
	}
	
	
	
	
	@Override
	public String getName()
	{
		return this.name;
	}

	protected boolean isMatchedName(String otherName)
	{
		if (this.name == null)
		{
			return true;
		}

		return this.name.equals(otherName);
	}
	
	protected static boolean areStringEqual(String s1, String s2)
	{
		if (s1 == null || s2 == null)
		{
			return s1 == s2;
		}
		
		return s1.equals(s2);
	}
	
	private String name;
}
