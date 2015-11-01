////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.items;

public class HelpChapter extends MatrixItem
{
	public HelpChapter(String str)
	{
		this.str = str;
	}

	@Override
	public String getItemName()
	{
		return this.str;
	}

	private String str = null;
}
