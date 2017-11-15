////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
