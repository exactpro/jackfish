////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.charts;

import java.io.IOException;
import java.util.List;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

public class GanntChartBuilder extends ChartBuilder
{
	public GanntChartBuilder(Table table, Parameters params) throws JFException
	{
		super(table, params);

		// TODO Auto-generated constructor stub
	}

	@Override
	public void report(ReportWriter writer, Integer integer) throws IOException
	{
		// TODO Auto-generated method stub
		
	}

	@Override
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
		// TODO Auto-generated method stub
		
	}

}
