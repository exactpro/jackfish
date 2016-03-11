////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Color;
import java.io.Serializable;

public class ValueAndColor implements Serializable
{

	private static final long serialVersionUID = -6432053858779071676L;

	public ValueAndColor(String value, Color color, Color backColor)
	{
		this.value 		= value;
		if (color != null)
		{
			this.color 	= new InnerColor(color);
		}
		if (backColor != null)
		{
			this.backColor = new InnerColor(backColor);
		}
	}

	public String getValue()
	{
		return value;
	}

	public Color getColor()
	{
		return color;
	}

	public Color getBackColor()
	{
		return backColor;
	}

	@Override
	public String toString()
	{
		return "value='" + value + '\'' +
				", color=" + color +
				", backColor=" + backColor;
	}

	private String value = null;
	private InnerColor color = null;
	private InnerColor backColor = null;
}
