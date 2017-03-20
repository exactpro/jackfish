////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import java.io.Serializable;
import java.util.Map;

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
	public boolean isMatched(Map<String, Object> map)
	{
		return true;
	}

	@Override
	public String toString()
	{
		return getClass().getSimpleName() + " [name=" + getName() + "]";
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "";
	}
}
