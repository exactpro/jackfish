/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;

import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = R.EMPTY_CONDITION_DESCRIPTION)
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
		return super.getSerializePrefix(this.getClass()) + start + getName() + finish;
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
