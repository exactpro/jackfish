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
