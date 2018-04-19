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

public enum ChartKind
{
	Line("Line chart"),
	Bar("Bar chart"),
	Pie("Pie chart"),
	Gannt("Gannt chart");

	private String description;

	ChartKind(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}

}
