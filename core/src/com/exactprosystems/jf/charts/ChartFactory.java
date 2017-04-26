////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.charts;

import com.exactprosystems.jf.api.app.ChartKind;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.common.UnknownChartKindException;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

import java.awt.*;
import java.util.Map;

public class ChartFactory
{
	private ChartFactory() {}
	
	public static ChartBuilder createChartBuilder(ChartKind chartKind, Table table, Map<String, Color> colors, Parameters params) throws JFException
	{
		switch (chartKind)
		{
			case Bar:	return new BarChartBuilder(table, params, colors);
			case Line:	return new LineChartBuilder(table, params, colors);
			case Pie:	return new PieChartBuilder(table, params, colors);
			case Gannt:	return new GanntChartBuilder(table, params, colors);
			default:	throw new UnknownChartKindException("" + chartKind);
		}
	}

	public static ChartBuilder createStubChartBuilder(ChartKind kind) throws JFException
	{
		switch (kind)
		{
			case Line:	return new LineChartBuilder();
			case Bar:	return new BarChartBuilder();
			case Pie:	return new PieChartBuilder();
			case Gannt:	return new GanntChartBuilder();
			default: throw new UnknownChartKindException("" + kind);
		}
	}
}
