////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.conditions;

import com.exactprosystems.jf.api.app.InnerColor;
import com.exactprosystems.jf.api.common.DescriptionAttribute;
import com.exactprosystems.jf.api.common.Str;

import java.awt.*;
import java.io.Serializable;
import java.util.Map;

@DescriptionAttribute(text = "Returns rows that satisfy the conditions applicable to color of text or background")
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
		return super.getSerializePrefix(this.getClass()) + start + getName() + separator + this.value + separator + this.foreground + finish;
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

	public String colorToString()
	{

		/*
			TODO be careful, when change type from InnerColor to Color.
			We need ovveride convert our color to value 'Color[1, 2, 3]'
			or change pattern on win side
			Now, pattern to parse color of win side : "Color\\[(\\d+),\\s?(\\d+),\\s?(\\d+)\\]"
		 */
		return this.value.toString();
	}
	
	private InnerColor value = null;

	private boolean foreground = true;
}
