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

package com.exactprosystems.jf.charts;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.JFException;
import com.exactprosystems.jf.api.error.common.NullParameterException;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

import java.awt.*;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BarChartBuilder extends ChartBuilder
{
	private static final String labelColumnName = "Labels";
	private static final String yAxisDescriptionName	= "YAxisDescription";

	private String yAxisDescription = "";
	private String labelColumn;

	public BarChartBuilder() throws JFException
	{
		super(null, null, null);
	}

	public BarChartBuilder(Table table, Parameters params, Map<String, Color> colorMap) throws JFException
	{
		super(table, params, colorMap);
		Object yAxisDesc = params.get(yAxisDescriptionName);
		if (yAxisDesc != null)
		{
			this.yAxisDescription = "" + yAxisDesc;
		}

		Object lblColumn = params.get(labelColumnName);
		if (lblColumn == null)
		{
			throw new NullParameterException(String.format(R.API_NULL_PARAMETER_EXCEPTION.get(), labelColumnName));
		}

		this.labelColumn = "" + lblColumn;
	}

	@Override
	public void report(ReportWriter writer, Integer count) throws IOException
	{
//		AtomicBoolean ab = new AtomicBoolean(false);
//		int headerSize = table.getHeaderSize();
//		IntStream.range(0, headerSize)
//				.mapToObj(table::getHeader)
//				.filter(this.labelColumn::equals)
//				.findFirst()
//				.ifPresent(s1 -> ab.set(true));
//
//		if (!ab.get())
//		{
//			throw new ChartException(String.format("Column with name %s is not presented", labelColumnName));
//		}
//
//		boolean allMatch = IntStream.range(0, table.getHeaderSize())	//for all headers
//				.mapToObj(table::getHeader)								//get header name
//				.filter(header -> !this.labelColumn.equals(header))			//exclude labelColumn
//				.allMatch(headerName ->
//						IntStream.range(0, this.table.size())				//for all rows
//								.mapToObj(this.table::get)					//get row
//								.allMatch(rt -> isNumber(rt.get(headerName).toString()))
//				);
//		if (!allMatch)
//		{
//			throw new ChartException("All values from columns, exclude label column, must be a number");
//		}

		String chartId = "chart_" + count;

		writer.fwrite("<div id='%s' class=container></div>", chartId);
		String data = generateData();
		writer.fwrite("<script>createBarChart('%s', %s, '%s', %s)</script>", chartId, data, this.yAxisDescription, createColors());

	}

	@Override
	public void helpToAddParameters(List<ReadableValue> list, Context context) throws Exception
	{
		list.add(new ReadableValue(labelColumnName, R.X_AXIS.get()));
		list.add(new ReadableValue(yAxisDescriptionName, R.Y_AXIS.get()));
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
