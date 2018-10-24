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
