/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.report;

import java.util.ArrayList;
import java.util.List;

public class ReportTable
{
	private boolean        decorated;
	private boolean        bordered;
	private String         title;
	private String         beforeTestcase;
	private String[]       columns;
	private int[]          widths;
	private List<Object[]> data;

	public ReportTable(String title, String beforeTestcase, boolean decorated, boolean bordered, int[] widths, String[] columns)
	{
		this.title = title;
		this.beforeTestcase = beforeTestcase;
		this.decorated = decorated;
		this.bordered = bordered;
		this.columns = columns;
		this.widths = widths;
	}

	public void addValues(Object... values)
	{
		if (this.data == null)
		{
			this.data = new ArrayList<>();
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

	public boolean isBordered()
	{
		return this.bordered;
	}

	public String[] getColumns()
	{
		return this.columns;
	}

	public int[] getWidths()
	{
		return this.widths;
	}

	public List<Object[]> getData()
	{
		return this.data;
	}
}
