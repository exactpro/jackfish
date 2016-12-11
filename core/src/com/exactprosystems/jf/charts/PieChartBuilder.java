////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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

public class PieChartBuilder extends ChartBuilder
{
	private Map<String, Color> colors;

	public PieChartBuilder(Table table, Parameters params, Map<String, Color> colors) throws JFException
	{
		super(table, params);
		this.colors = colors;
	}

	@Override
	public void report(ReportWriter writer, Integer id) throws IOException
	{
//		AtomicBoolean ab = new AtomicBoolean(false);
//		int headerSize = table.getHeaderSize();
//		IntStream.range(0, headerSize)
//				.mapToObj(table::getHeader)
//				.filter(valueColumnName::equals)
//				.findFirst()
//				.ifPresent(s1 -> ab.set(true));
//
//		if (!ab.get())
//		{
//			throw new ChartException(String.format("Column with name %s is not presented", valueColumnName));
//		}
//		if (!table.stream().allMatch(rt -> isNumber(rt.get(valueColumnName).toString())))
//		{
//			throw new ChartException(String.format("All values from column %s must be a number", valueColumnName));
//		}

//		if (labelColumn == null)
//		{
//			this.labelColumnName = IntStream.range(0, headerSize)
//					.mapToObj(table::getHeader)
//					.filter(s -> !valueColumnName.equals(s))
//					.findFirst()
//					.orElseThrow(() -> new ChartException("Pie chart can't be drawing from table with one column"));
//		}
//		else
//		{

//		}

		String chartId = "chart_" + id;
		writer.fwrite("<div id='%s' class=container></div>", chartId);
		String data = createData();
		//TODO THINK ABOUT IT
		String colors = createColors();
		writer.fwrite("<script>createPieChart('%s',%s, %s)</script>", chartId, data, colors);
	}

	private String createColors()
	{
		if (this.colors == null)
		{
			return "undefined";
		}
		StringBuilder sbColors = new StringBuilder("{");
		String colors = this.colors.entrySet()
				.stream()
				.map(e -> String.format("'%s' : '%s'", e.getKey(), getHTMLColorString(e.getValue())))
				.collect(Collectors.joining(","));
		sbColors.append(colors);
		sbColors.append("}");
		return sbColors.toString();
	}

	private String createData()
	{
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		String separator = "";
		for (int i = 0; i < this.table.getHeaderSize(); i++)
		{
			String label = this.table.getHeader(i);
			String value = String.valueOf(this.table.get(0).get(label));
			sb.append(separator).append(String.format("{'value' : %s, 'label' : '%s'}", value, label));
			separator = ",";
		}
		sb.append("]");
		return sb.toString();
	}

	@Override
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
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
}