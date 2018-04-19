/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
