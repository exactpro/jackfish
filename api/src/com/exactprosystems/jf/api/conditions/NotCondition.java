////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.util.Map;

public class NotCondition extends Condition
{
	private static final long serialVersionUID = 1308424226945776212L;

	public NotCondition(Condition cond)
	{
		super(null);
		this.cond = cond;
	}

	@Override
	public String serialize()
	{
		return "!" + start + getName() + separator + this.cond.serialize() + finish;
	}
	
	@Override
	public String toString()
	{
		return "Not " + this.cond.toString();
	}
	
	@Override
	public String getName()
	{
		return this.cond.getName();
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return !this.cond.isMatched(map);
	}

	@Override
	public boolean isMatchedName(String otherName)
	{
		return this.cond.isMatchedName(otherName);
	}
	
	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		return !this.cond.isMatched(otherName, otherValue);
	}

    @Override
    public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
    {
    	return isMatched(otherName, otherValue1);
    }
    
    @Override
    public String explanation(String name, Object actualValue)
    {
    	return " !(" + cond.explanation(name, actualValue) + ")";
    }

	private Condition cond;
}
