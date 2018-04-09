/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.charts;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class GanntChartBuilder extends ChartBuilder
{
	public GanntChartBuilder() throws JFException
	{
		super(null, null, null);
	}

	public GanntChartBuilder(Table table, Parameters params, Map<String, Color> colorMap) throws JFException
	{
		super(table, params, colorMap);

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
