////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.util.ArrayList;
import java.util.List;

public class ReportTable
{
	public ReportTable(String title, String beforeTestcase, boolean decorated, int quotedSince, int[] widths, String[] columns)
	{
		this.title = title;
		this.beforeTestcase = beforeTestcase;
		this.decorated = decorated;
		this.columns = columns;
	}

	public void addValues(Object ... values)
	{
		if (this.data == null)
		{
			this.data = new ArrayList<Object[]>();
		}
		
		this.data.add(values);
	}
	
	public String getTitle()
	{
		return this.title;
	}
	
	public String getBeforeTestcase()
	{
		return this.beforeTestcase;
	}
	
	public boolean isDecorated()
	{
		return this.decorated;
	}

	public String[] getColumns()
	{
		return this.columns;
	}

	public List<Object[]> getData()
	{
		return this.data;
	}

	protected boolean decorated;
	protected String title;
	protected String beforeTestcase;
	protected String[] columns;
	protected List<Object[]> data;
}
