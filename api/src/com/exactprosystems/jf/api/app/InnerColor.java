////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Color;
import java.io.Serializable;

public class InnerColor extends Color implements Serializable
{

	private static final long serialVersionUID = -8046577111230308405L;

	public InnerColor(Color color)
	{
		super(color.getRed(),color.getGreen(),color.getBlue(),color.getAlpha());
	}
	
	@Override
	public String toString()
	{
		return "Color[" + this.getRed() + ", " + this.getGreen() + ", " + this.getBlue() + "]";
	}
}
