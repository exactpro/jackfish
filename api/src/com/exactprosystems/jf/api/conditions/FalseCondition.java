////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = "Returns false for any matches")
public class FalseCondition extends Condition  implements Serializable
{

	private static final long serialVersionUID = 768671847853009009L;

	public FalseCondition()
	{
		super(null);
	}

	public FalseCondition(String name)
	{
		super(name);
	}

	@Override
	public String serialize()
	{
		return super.getSerializePrefix(this.getClass()) + start + getName() + finish;
	}

	@Override
	public boolean isMatched(Map<String, Object> map)
	{
		return false;
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
