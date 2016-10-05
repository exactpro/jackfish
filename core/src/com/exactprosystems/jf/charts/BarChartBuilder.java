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
import com.exactprosystems.jf.api.error.common.ChartException;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BarChartBuilder extends ChartBuilder
{
	private static final String labelColumnName = "Labels";
	private static final String yAxisDescriptionName	= "YAxisDescription";

	private String yAxisDescription = "";
	private String labelColumn;

	public BarChartBuilder(Table table, Parameters params) throws JFException
	{
		super(table, params);
		Object yAxisDesc = params.get(yAxisDescriptionName);
		if (yAxisDesc != null)
		{
			this.yAxisDescription = "" + yAxisDesc;
		}

		Object lblColumn = params.get(labelColumnName);
		if (lblColumn == null)
		{
			throw new NullParameterException(String.format("Parameter %s can't be null", labelColumnName));
		}

		this.labelColumn = "" + lblColumn;
		AtomicBoolean ab = new AtomicBoolean(false);
		int headerSize = table.getHeaderSize();
		IntStream.range(0, headerSize)
				.mapToObj(table::getHeader)
				.filter(this.labelColumn::equals)
				.findFirst()
				.ifPresent(s1 -> ab.set(true));

		if (!ab.get())
		{
			throw new ChartException(String.format("Column with name %s is not presented", labelColumnName));
		}

		boolean allMatch = IntStream.range(0, table.getHeaderSize())	//for all headers
				.mapToObj(table::getHeader)								//get header name
				.filter(header -> !lblColumn.equals(header))			//exclude labelColumn
				.allMatch(headerName ->
						IntStream.range(0, this.table.size())				//for all rows
								.mapToObj(this.table::get)					//get row
								.allMatch(rt -> isNumber(rt.get(headerName).toString()))
				);
		if (!allMatch)
		{
			throw new ChartException("All values from columns, exclude label column, must be a number");
		}
	}

	@Override
	public void report(ReportWriter writer, Integer count) throws IOException
	{
		String chartId = "chart_" + count;

		writer.fwrite("<div id='%s' class=container></div>", chartId);
		String data = generateData();
		writer.fwrite("<script>createBarChart('%s', %s, '%s')</script>", chartId, data, this.yAxisDescription);

	}

	@Override
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
		list.add(new ReadableValue(labelColumnName, "X axis labels for bar chart"));
		list.add(new ReadableValue(yAxisDescriptionName, "Y axis description for bar chart"));
	}

	private String generateData()
	{
		return IntStream.range(0, this.table.size())
				.mapToObj(this.table::get)
				.map(rt -> rt.entrySet()
						.stream()
						.map(entry -> {
							String columnName = entry.getKey();
							if (columnName.equals(this.labelColumn))
							{
								return String.format("\"%s\":\"%s\"", "label", "" + entry.getValue());
							}
							else
							{
								return String.format("\"%s\":%s", columnName, "" + entry.getValue());
							}
						})
						.collect(Collectors.joining(",", "{", "}"))
				)
				.collect(Collectors.joining(",", "[", "]"));
	}

}
