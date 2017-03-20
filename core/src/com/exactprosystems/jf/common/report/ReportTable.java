////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class ReportTable
{
	public ReportTable(String title, String beforeTestcase, boolean decorated, int quotedSince, int[] widths, String[] columns)
	{
		this.title = title;
		this.beforeTestcase = beforeTestcase;
		this.decorated = decorated;
		this.columns = columns;
		//sum of widths cant be more 100
		if (widths != null && IntStream.of(widths).sum() > 100){
			int[] n = {};
			for (int i : widths){
				n = addElement(n, i / IntStream.of(widths).sum());
			}
			this.percents = n;
		} else {this.percents = widths;}
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

	public int[] getPercents() {
		return this.percents;
	}

	private static int[] addElement(int[] a, int e) {
		a  = Arrays.copyOf(a, a.length + 1);
		a[a.length - 1] = e;
		return a;
	}

	protected boolean decorated;
	protected String title;
	protected String beforeTestcase;
	protected String[] columns;
	protected List<Object[]> data;
	protected int[] percents;
}
