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

public class PieChartBuilder extends ChartBuilder
{
	private static final String valuesColumnName = "Values";
	private static final String labelsColumnName = "Labels";

	private String valueColumnName;
	private String labelColumnName;

	public PieChartBuilder(Table table, Parameters params) throws JFException
	{
		super(table, params);
		Object valueColumn = params.get(valuesColumnName);
		if (valueColumn == null)
		{
			throw new NullParameterException(String.format("Parameter %s can't be null", valuesColumnName));
		}
		
		this.valueColumnName = "" + valueColumn;
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
//		Object labelColumn = params.get(labelsColumnName);
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
//			this.labelColumnName = "" + labelColumn;
//		}

		
		String chartId = "chart_" + id;
		writer.fwrite("<div id='%s' class=container></div>", chartId);
		String data = "[" + this.table.stream().map(rt -> String.format("{'value' : %s, 'label' : '%s'}", rt.get(valueColumnName), rt.get(labelColumnName))).collect(Collectors.joining(",")) + "]";
		writer.fwrite("<script>createPieChart('%s',%s)</script>", chartId, data);
	}

	@Override
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
		list.add(new ReadableValue(valuesColumnName, "Column name, which describe values for pie chart"));
		list.add(new ReadableValue(labelsColumnName, "Column name, which describe labels for pie chart"));
	}
}