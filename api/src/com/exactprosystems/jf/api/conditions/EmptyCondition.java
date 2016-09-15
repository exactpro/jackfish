////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.io.Serializable;

public class EmptyCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = -7391436700378183892L;

	public EmptyCondition(String name)
	{
		super(name);
	}

	@Override
	public String serialize()
	{
		return start + simpleName() + separator + getName() + finish;
	}
	
	@Override
	public String toString()
	{
		return EmptyCondition.class.getSimpleName() + " [name=" + getName() + "]";
	}

	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		return otherValue == null || ("" + otherValue).isEmpty();
	}

	@Override
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
	{
		return false;
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "'" + actualValue + "' is not empty"; 
	}
}
