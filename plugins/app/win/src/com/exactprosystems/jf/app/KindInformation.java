////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

public enum KindInformation
{
	Value(0), Color(1), BackColor(2);
	
	private KindInformation(int id)
	{
		this.id = id;
	}
	
	public int getId()
	{
		return this.id;
	}
	
	private int id;
}
