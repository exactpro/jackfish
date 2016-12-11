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
		ChartBuilder chart = null;
		switch (chartKind)
		{
		case Bar:
			chart = new BarChartBuilder(table, params);
			break;
			
		case Line:
			chart = new LineChartBuilder(table, params);
			break;
			
		case Pie:
			chart = new PieChartBuilder(table, params, colors);
			break;
			
		case Gannt:
			chart = new GanntChartBuilder(table, params);
			break;

		default:
			throw new UnknownChartKindException("" + chartKind);
		}

		return chart;
	}
}
