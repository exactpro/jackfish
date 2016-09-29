////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.api.app;

@Deprecated
public enum HistogramMetric
{
	Find("Find"),
	Text("Enter text"),
	Click("Mouse click"),
	Move("Mouse move");

	private String description;

	HistogramMetric(String description)
	{
		this.description = description;
	}

	public String getDescription()
	{
		return description;
	}
}
