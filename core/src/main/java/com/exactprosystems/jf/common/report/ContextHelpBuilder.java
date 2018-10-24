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

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;
import com.exactprosystems.jf.tool.settings.Theme;

import java.io.IOException;
import java.util.Date;

public class ContextHelpBuilder extends ReportBuilder
{
	private static final long serialVersionUID = -4131612316361245162L;

	public ContextHelpBuilder(Date currentTime) throws IOException
	{
		super(null, null, currentTime);
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		return HTMLhelper.htmlMarker(value);
	}

	@Override
	protected String replaceMarker(String marker)
	{
		return HTMLhelper.htmlMarker(marker);
	}

	@Override
	protected String generateReportName(String outputPath, String matrixName, String suffix, Date date)
	{
		return "";
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		return name;
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return "";
	}

	@Override
	protected String decorateGroupCell(String content, int level, boolean isNode)
	{
		return "";
	}

	@Override
	protected String generateReportDir(String matrixName, Date date)
	{
		return null;
	}

	@Override
	protected void putMark(ReportWriter writer, String mark)
	{
	}

	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
		writer.fwrite(
				"<html>\n"
				+ "<head>\n"
				+ "<title>Help</title>\n"
				+ "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n");

		writer.fwrite("<style>\n");
		if (Theme.currentTheme().equals(Theme.WHITE))
		{
			writer.include(this.getClass().getResourceAsStream("white-help.css"));
		}
		else
		{
			writer.include(this.getClass().getResourceAsStream("dark-help.css"));
		}
		writer.fwrite("</style>\n");

		writer.fwrite("</head>\n"
				+ "<body>\n");
	}

	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName)
	{
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line)
	{
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer)
	{
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date)
	{
	}


	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id)
	{
		//do u want to change this function? check help for plugins
		//writer.fwrite("<h2>%s</h2>\n", postProcess(item.getItemName()));
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException
	{
		writer.fwrite("<span class='tableTitle'>%s</span><br>", this.postProcess(title));

		switch (reportMode)
		{
			case AsEmbeddedImage:
				writer.fwrite("<img src='data:image/jpeg;base64,%s' class='img'/><br>", embedded);
				break;

			case AsImage:
				writer.fwrite("<img src='%s' class='img'/><br>", fileName);
				break;

			case AsLink:
				writer.fwrite("<a href=" + fileName + ">Image</a><br>");
				break;

			default:
				break;
		}
	}

	@Override
	protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title)
	{

	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(string);
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot)
	{
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
		writer.fwrite("</body>\n");
		writer.fwrite("</html>");
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite(
				"<span class='tableTitle'>%s</span><br>",
				this.postProcess(tableTitle));

		if (table.isBordered())
		{
			writer.fwrite(
					"<table width='100%%' border='1' bordercolor='#000000' cellpadding='3' cellspacing='0'>\n" +
					"<tr style='font-weight: bold;'>\n");
		}
		else
		{
			writer.fwrite(
					"<table width='100%%' border='0' cellpadding='3' cellspacing='0'>\n" +
					"<tr style='font-weight: bold;'>\n");
		}

		for (int i = 0; i < columns.length; i++)
		{
			if (percents == null || percents.length < i || percents[i] <= 0)
			{
				writer.fwrite("<td>%s", columns[i]);
			}
			else
			{
				writer.fwrite("<td width='%d%%'>%s", percents[i], columns[i]);
			}
		}

		writer.fwrite("\n");
	}

	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException
	{
		if (value != null)
		{
			writer.fwrite("<tr>");
			int count = 0;
			for (Object obj : value)
			{
				writer.fwrite("<td>%s", ReportHelper.objToString(obj, count >= quotes));
				count++;
			}
			writer.fwrite("\n");
		}
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("</table>\n");
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder)
	{

	}
}
