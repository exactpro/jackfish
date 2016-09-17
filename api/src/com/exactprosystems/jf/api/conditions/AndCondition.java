////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AndCondition extends Condition
{
	private static final long serialVersionUID = -7043230215754395460L;

	public AndCondition(Condition ... cond) throws Exception
	{
		super(null);
		
		this.cond = cond == null ? Collections.emptyList() : Arrays.asList(cond);
	}

	@Override
	public String serialize()
	{
		return "&" + start + this.cond.stream().map(s -> s.serialize()).reduce((s1,s2) -> s1 + separator + s2).orElse("") + finish; 
	}
	
	@Override
	public String toString()
	{
		return this.cond.stream().map(s -> s.toString()).reduce((s1,s2) -> s1 + " AND " + s2).orElse("");
	}
	
	@Override
	public String getName()
	{
		return this.cond.stream().map(s -> s.getName()).findFirst().orElse("");
	}
	
	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		return this.cond.stream().map(c -> c.isMatched(otherName, otherValue)).reduce((s1, s2) -> s1 && s2).orElse(false);
	}
	
    @Override
    public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
    {
    	return isMatched(otherName, otherValue1);
    }

    @Override
    public String explanation(String name, Object actualValue)
    {
		return this.cond.stream().map(s -> "(" + s.explanation(name, actualValue) + ")").reduce((s1,s2) -> s1 + " & " + s2).orElse("");
    }
    
	private List<Condition> cond;
}
