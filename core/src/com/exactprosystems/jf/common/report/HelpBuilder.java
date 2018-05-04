/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;
import com.exactprosystems.jf.functions.ContentItem;
import com.exactprosystems.jf.tool.settings.Theme;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

public class HelpBuilder extends ReportBuilder {

	private static final long serialVersionUID = -5583389098545753476L;

	public HelpBuilder(Date currentTime) throws IOException
	{
		super(".", "help", currentTime);
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		String rgbColor;
		switch (style)
		{
			case "BLACK":
				rgbColor = "#000000";
				break;
			case "BLUE":
				rgbColor = "#0000FF";
				break;
			case "CYAN":
				rgbColor = "#00FFFF";
				break;
			case "DARK_GRAY":
				rgbColor = "#A9A9A9";
				break;
			case "GRAY":
				rgbColor = "#808080";
				break;
			case "GREEN":
				rgbColor = "#00FF00";
				break;
			case "LIGHT_GRAY":
				rgbColor = "#D3D3D3";
				break;
			case "MAGENTA":
				rgbColor = "#FF00FF";
				break;
			case "ORANGE":
				rgbColor = "#FFA500";
				break;
			case "PINK":
				rgbColor = "#FFC0CB";
				break;
			case "RED":
				rgbColor = "#FF0000";
				break;
			case "WHITE":
				rgbColor = "#FFFFFF";
				break;
			case "Failed":
				rgbColor = "#FF0000";
				break;
			default:
				rgbColor = "#000000";
		}

		return String.format("<span style=\"color: %s\">%s</span>", rgbColor, value);
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		return String.format("<a href='#MoveTo%s'>%s</a>", name, link);
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return "";
	}

	@Override
	protected String decorateGroupCell(String content, int level, boolean isNode)
	{
		return content;
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
	protected String generateReportDir(String matrixName, Date date)
	{
		return null;
	}

	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
		writer.newline().fwrite("<div id=\"MoveTo%s\"></div>", mark).newline();
	}

	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
		writer.fwrite("<!DOCTYPE html>").newline()
				.fwrite("<html>").newline()
				.fwrite("<head>").newline()
				.fwrite("<title>Help</title>").newline()
				.fwrite("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>").newline();

		writer.fwrite("<script type='text/javascript'>").newline();
		writer.include(this.getClass().getResourceAsStream("jquery-3.1.1.min.js"));
		writer.newline().fwrite("</script>").newline();

		writer.fwrite("<script type='text/javascript'>").newline();
		writer.include(this.getClass().getResourceAsStream("bootstrap.min.js"));
		writer.newline().fwrite("</script>").newline();

		writer.fwrite("<style>").newline();
		writer.include(this.getClass().getResourceAsStream("bootstrap.min.css"));
		writer.newline().fwrite("</style>").newline();

		writer.fwrite("<style>").newline();
		writer.include(this.getClass().getResourceAsStream(Theme.currentTheme() == Theme.WHITE ? "white-help.css" : "dark-help.css"));
		writer.newline().fwrite("</style>").newline();

		writer.fwrite("</head>").newline()
				.fwrite("<body>").newline()
				.fwrite("<div class='searchDiv'>").newline()
				.fwrite("<span class='searchControls'>").newline()
				.fwrite("<input class='searchInput' type='text' placeholder='Search'/>").newline()
				.fwrite("<button class='nonActiveBtn' id='btnPrev'>&#923;</button>").newline()
				.fwrite("<button class='nonActiveBtn' id='btnNext'>V</button>").newline()
				.fwrite("</span>").newline()
				.fwrite("<label class='searchLabel'></label>").newline()
				.fwrite("<td><h0>Version <td>%s</h0></td>", VersionInfo.getVersion()).newline()
				.fwrite("</div>").newline()
				.fwrite("<div class='container-fluid'>").newline()
				.fwrite("<div class='row'>").newline()
				.fwrite("<div class='col-sm-3 menuCont'>").newline()
				.fwrite("<div class='mainMenu'>").newline()
				.fwrite("<ul class='nav nav-pills nav-stacked'>").newline()
				.fwrite("<div id='contentPart'></div>").newline()
				.fwrite("</ul>").newline()
				.fwrite("</div>").newline()
				.fwrite("</div>").newline()
				.fwrite("<div class='col-sm-9 helpViewer'>").newline();
	}

	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrix)
	{
		// nothing to do
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line)
	{
		// nothing to do
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer)
	{
		// nothing to do
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date)
	{
		// nothing to do
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
		writer.fwrite("</div>").newline()
				.fwrite("</div>").newline()
				.fwrite("</div>").newline();

		writer.fwrite("<script type='text/javascript'>").newline();
		writer.include(this.getClass().getResourceAsStream("help.js"));
		writer.newline().fwrite("</script>").newline();

		writer.fwrite("</body>\n").newline()
				.fwrite("</html>").newline();
	}

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id)
	{

	}

	@Override
	protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title) throws IOException
	{
		String collect = content.stream()
				.map(ContentItem::toString)
				.collect(Collectors.joining());

		writer.fwrite("<script type='text/javascript'>").newline()
				.fwrite("document.getElementById('contentPart').innerHTML = \"%s\"",collect.replaceAll("\\n", "")).newline()
				.fwrite("</script>");
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(string + "<br>").newline();
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException
	{
		writer.fwrite("<img src=\"%s\" alt=\"%s\" >", fileName, title).newline();
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time, ImageWrapper screenshot)
	{
		//nothing to do
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		boolean isColumnWidthSet = percents.length != 0 && columns.length == percents.length;

		if (!Str.IsNullOrEmpty(tableTitle))
		{
			writer.newline()
					.fwrite("<div id=\"%s\"></div>", tableTitle.replaceAll("\\s+", "").toLowerCase()).newline()
					.fwrite("<h3>%s</h3>", tableTitle).newline();
		}
		if (!Str.IsNullOrEmpty(table.getTitle()))
		{
			writer.fwrite("<div id=\"%s\">", table.getTitle().replaceAll("\\s+", "").toLowerCase() + "doublescrolljs").newline();
		}
		writer.fwrite("<table class='table table-bordered table-condensed'>\n");
		if (isColumnWidthSet)
		{
			writer.fwrite("<colgroup>").newline();
			for (int percent : percents)
			{
				writer.fwrite("<col span=\"1\" style=\"width: %d%%;\">", percent).newline();
			}
			writer.fwrite("</colgroup>").newline();
		}
		writer.fwrite("<thead>").newline()
				.fwrite("<tr>").newline();

		for (String column : columns)
		{
			writer.fwrite("<th>%s</th>", column).newline();
		}

		writer.fwrite("</tr>").newline()
				.fwrite("</thead>").newline();
	}

	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException
	{
		if (value != null)
		{
			writer.fwrite("<tr>").newline();
			int count = 0;
			for (Object obj : value)
			{
				writer.fwrite("<td>%s</td>", ReportHelper.objToString(obj, count >= quotes)).newline();
				count++;
			}
			writer.fwrite("</tr>").newline();
		}
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("</table>").newline();
		if (!Str.IsNullOrEmpty(table.getTitle()))
		{
			writer.fwrite("</div>").newline();
		}
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder)
	{

	}
}
