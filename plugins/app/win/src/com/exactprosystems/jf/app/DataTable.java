////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.Arrays;

public class DataTable
{
	public DataTable()
	{
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(getClass().getSimpleName()).append(" {\n");
		if (this.headers != null)
		{
			sb.append(Arrays.toString(this.headers)).append("\n");
		}
		if (this.data != null)
		{
			for(String[] row : this.data)
			{
				if (row != null)
				{
					sb.append(Arrays.toString(row)).append("\n");
				}
			}
		}
		sb.append('}');
		return sb.toString();
	}

	public void setHeades(String[] headers)
	{
		this.headers = headers;
	}

	public String[] getHeades()
	{
		return this.headers;
	}

	public String[][] getData()
	{
		return this.data;
	}
	
	public void setData(String[][] data)
	{
		this.data = data;
	}
	
	private String[] headers;

	private String[][] data;
}
