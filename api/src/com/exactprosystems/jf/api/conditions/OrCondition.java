////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

public class OrCondition extends Condition
{
	private static final long serialVersionUID = 7676146584103803972L;

	public OrCondition(Condition cond1, Condition cond2) throws Exception
	{
		super(null);
		
		this.cond1 = cond1;
		this.cond2 = cond2;
	}

	@Override
	public String toString()
	{
		return this.cond1.toString() + " OR " + this.cond2.toString();
	}
	
	@Override
	public String getName()
	{
		return this.cond1.getName();
	}
	
	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		return this.cond1.isMatched(otherName, otherValue) || this.cond2.isMatched(otherName, otherValue);
	}

    @Override
    public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
    {
        return this.cond1.isMatched2(otherName, otherValue1, otherValue2) || this.cond2.isMatched2(otherName, otherValue1,otherValue2);
    }

    @Override
    public String explanation(String name, Object actualValue)
    {
    	return "(" + cond1.explanation(name, actualValue) + ") | (" + cond2.explanation(name, actualValue) + ")";
    }
    
	private Condition cond1;
	private Condition cond2;
}
