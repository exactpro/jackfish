////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.common.highlighter;

public class StyleWithRange
{
	private String style;
	private int range;

	public StyleWithRange(String style, int range)
	{
		this.style = style;
		this.range = range;
	}

	public String getStyle()
	{
		return style;
	}

	public int getRange()
	{
		return range;
	}

	@Override
	public String toString()
	{
		return this.style + " [" + this.range + "]";
	}
}
