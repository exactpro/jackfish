/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
