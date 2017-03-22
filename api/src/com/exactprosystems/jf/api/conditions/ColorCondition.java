////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.app.InnerColor;
import com.exactprosystems.jf.api.common.Str;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

public class ColorCondition extends Condition implements Serializable
{

	private static final long serialVersionUID = -2853180365570996848L;

	public ColorCondition(String name, Color value, boolean foreground)
	{
		super(name);
		this.value = value == null ? null : new InnerColor(value);
		this.foreground = foreground;
	}

	public ColorCondition(String name, Color value)
	{
		this(name, value, true);
	}

	@Override
	public String serialize()
	{
		return "C" + start + getName() + separator + this.value + separator + this.foreground + finish;
	}
	
	@Override
	public String toString()
	{
		return ColorCondition.class.getSimpleName() + " [name=" + getName() + ", value=" + value  + ", foreground=" + foreground + "]";
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

		return value instanceof Color && this.value.equals(value);
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return String.valueOf(this.value) + " != " + actualValue;
	}

	public Color getColor()
	{
	    if (this.value != null)
	    {
	        return this.value;
	    }
	    return Color.black;
	}
	
	private InnerColor value = null;

	private boolean foreground = true;
}
