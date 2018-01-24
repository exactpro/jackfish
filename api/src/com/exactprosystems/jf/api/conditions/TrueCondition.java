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
import com.exactprosystems.jf.api.common.i18n.R;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = R.TRUE_CONDITION_DESCRIPTION)
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
		return super.getSerializePrefix(this.getClass()) + start + getName() + finish;
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
