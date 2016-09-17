////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.io.Serializable;

public class TrueCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 768671847855709009L;

	public TrueCondition()
	{
		super(null);
	}

	public TrueCondition(String name)
	{
		super(name);
	}

	@Override
	public String serialize()
	{
		return "E" + start + getName() + finish;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + "]";
	}

	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		return true;
	}

	@Override
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
	{
		return true;
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "";
	}
}
