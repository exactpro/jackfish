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
import java.util.stream.Collectors;

public abstract class ChartBuilder
{
	ChartBuilder(Table table, Parameters params, Map<String,Color> colorMap) throws JFException
	{
		this.table = table;
		this.params = params;
		this.colorMap = colorMap;
	}

	public abstract void report(ReportWriter writer, Integer integer) throws IOException;

	public abstract void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception;

	protected static boolean isNumber(String s)
	{
		try
		{
			Double.parseDouble(s);
			return true;
		}
		catch (Exception e)
		{
			return false;
		}
	}

	protected String createColors()
	{
		if (this.colorMap == null)
		{
			return "undefined";
		}
		StringBuilder sbColors = new StringBuilder("{");
		String colors = this.colorMap.entrySet()
				.stream()
				.map(e -> String.format("'%s' : '%s'", e.getKey(), getHTMLColorString(e.getValue())))
				.collect(Collectors.joining(","));
		sbColors.append(colors);
		sbColors.append("}");
		return sbColors.toString();
	}

	private static String getHTMLColorString(Color color) {
		String red = Integer.toHexString(color.getRed());
		String green = Integer.toHexString(color.getGreen());
		String blue = Integer.toHexString(color.getBlue());

		return "#" +
				(red.length() == 1? "0" + red : red) +
				(green.length() == 1? "0" + green : green) +
				(blue.length() == 1? "0" + blue : blue);
	}

	protected Map<String, Color> colorMap;
	protected Table table;
	protected Parameters params;
}
