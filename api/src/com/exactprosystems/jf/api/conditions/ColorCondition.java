////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.app.InnerColor;

import java.awt.*;
import java.io.Serializable;

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
	public String toString()
	{
		return ColorCondition.class.getSimpleName() + " [name=" + getName() + ", value=" + value  + ", foreground=" + foreground + "]";
	}

	@Override
	public boolean isMatched(String otherName, Object otherValue)
	{
		if (!isMatchedName(otherName))
		{
			return true;
		}
		
		if (otherValue instanceof Color)
		{
			return this.value.equals(otherValue);
		}

		return false;
	}

	@Override
	public boolean isMatched2(String otherName, Object otherValue1, Object otherValue2)
	{
		if (!isMatchedName(otherName))
		{
			return true;
		}
		
		if (this.foreground)
		{
			if (otherValue1 == null)
			{
				return this.value == otherValue1;
			}
			
			if (this.value == null)
			{
				return false;
			}
			
			if (otherValue1 instanceof Color)
			{
				return this.value.equals(otherValue1);
			}
		}
		else
		{
			if (otherValue2 == null)
			{
				return this.value == otherValue2;
			}
			
			if (this.value == null)
			{
				return false;
			}
			
			if (otherValue2 instanceof Color)
			{
                return this.value.equals(otherValue2);
			}
		}
		
		return false;
	}

	@Override
	public String explanation(String name, Object actualValue)
	{
		return String.valueOf(this.value) + " != " + actualValue;
	}

	private InnerColor value = null;

	private boolean foreground = true;
}
