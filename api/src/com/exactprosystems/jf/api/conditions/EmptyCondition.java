////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.Str;

import java.io.Serializable;
import java.util.Map;

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
		return "E" + start + getName() + finish;
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
		return value == null || ("" + value).isEmpty();
	}

	@Override
	public String toString()
	{
		return EmptyCondition.class.getSimpleName() + " [name=" + getName() + "]";
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return "'" + actualValue + "' is not empty"; 
	}
}
