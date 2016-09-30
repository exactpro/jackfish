////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.charts;

import java.util.List;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

public class ChartBuilder
{
	ChartBuilder(Table table, Parameters params)
	{
		this.table = table;
		this.params = params;
	}
	
	public void report(ReportWriter writer)
	{
		
	}
	
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
		
	}

	
	protected Table table;
	protected Parameters params;
}
