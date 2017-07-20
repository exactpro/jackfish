////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class HTMLReportBuilder extends ReportBuilder
{
	private static final long serialVersionUID = 8277698425881479782L;

	private static Integer chartCount = 0;
	private static final String reportExt = ".html";
	private static final DateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");
	private final int columnCount = 7;

	private StringWriter jsWriter = new StringWriter();

	public HTMLReportBuilder()
	{
		super();
	}

	public HTMLReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		super(outputPath, matrixName, currentTime);

	}

	@Override
	protected String postProcess(String result)
	{
		return super.postProcess(result);
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		return String.format("<div class=\"%s\">%s</div>", style, value);
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		return String.format("<a href=\"%s\" target=\"_blank\">%s</a>",
				link,
				name);
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return String.format("<a href=\"\" onclick=\"obj=this.parentNode.childNodes[1].style; "
						+ "obj.display=(obj.display!='block')?'block':'none'; return false;\">%s</a>"
						+ "<div style='display: none;'>%s</div>",
				name,
				content);
	}

	@Override
	protected String decorateGroupCell(String content, int level, boolean isNode)
	{
		String res = null;
		if (isNode)
		{
			res = String.format("<a href=\"javascript:void(0)\" indent-level=\"%d\" class=\"group\">%s</a>",
					level,
					content);
		}
		else
		{
			res = String.format("<span indent-level=\"%d\" class=\"group\">%s</span>",
					level,
					content);
		}
		return res;
	}

	@Override
	protected String replaceMarker(String marker)
	{
		return HTMLhelper.htmlMarker(marker);
	}

	@Override
	protected String generateReportName(String outputPath, String matrixName, String suffix, Date date)
	{
		if (matrixName.toLowerCase().endsWith(Configuration.matrixExt))
		{
			matrixName = matrixName.substring(0, matrixName.length()-Configuration.matrixExt.length());
		}
		synchronized (dateTimeFormatter)
		{
			return outputPath + File.separator + dateTimeFormatter.format(date) + matrixName + suffix + reportExt;
		}
	}

	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
		if (!Str.IsNullOrEmpty(mark))
		{
			writer.fwrite(String.format("<div id=\"TC_%s\" ></div>", mark));
		}
	}

	@Override
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
		if (matrixName.toLowerCase().endsWith(Configuration.matrixExt))
		{
			matrixName = matrixName.substring(0, matrixName.length()-Configuration.matrixExt.length());
		}
		synchronized (dateTimeFormatter)
		{
			return dateTimeFormatter.format(date) + matrixName;
		}
	}

	//region Global report
	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
		this.jsWriter.fwrite("<script type='text/javascript'>\n");
		writer.fwrite(
				 "<html>\n"
				+ "<head>\n"
				+ "<script type='text/javascript'>\n"
				+ "    var timerStart = Date.now();\n"
				+ "</script>"
				+ "<title>Report</title>\n"
				+ "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n");


		writer.fwrite("<style>\n");
		writer.include(getClass().getResourceAsStream("style.css"));
		writer.fwrite("</style>\n");

		writer.fwrite("<script>");
		writer.include(getClass().getResourceAsStream("jquery-1.8.3.min.js"));
		writer.include(getClass().getResourceAsStream("reports.js"));
		writer.include(getClass().getResourceAsStream("charts.js"));
		writer.include(getClass().getResourceAsStream("d3.min.js"));
		writer.fwrite("</script>");


		writer.fwrite(
				  "</head>\n"
				+ "<body>\n"
				+ "<h1>EXECUTION REPORT</h1>\n"
				+ "<table id='tableInfo' class='table'>\n");

		writer.fwrite("<tr><td><span id='name'></span>\n");
		writer.fwrite("<tr><td>Version <td>%s\n", Str.asString(version));
		writer.fwrite("<tr><td>Start time: <td><span id='startTime'>Calculating...</span>\n");
		writer.fwrite("<tr><td>Finish time: <td><span id='finishTime'>Calculating...</span>\n");
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
		writer.fwrite("<tr>");
		writer.fwrite("<td colspan='2'>");
		writer.fwrite("<button class='btn btn-info filterTotal' type='button'>Executed : <span id='exec' class='badge'>0</span></button>");
		writer.fwrite("<button class='btn btn-success filterPassed' type='button'>Passed : <span id='pass' class='badge'>0</span></button>");
		writer.fwrite("<button class='btn btn-danger filterFailed' type='button'>Failed : <span id='fail' class='badge'>0</span></button>");
		writer.fwrite("<button class='btn btn-default filterExpandAllFailed' type='button'><span class='text-danger'>Expand all failed</span></button>");
		writer.fwrite("<button class='btn btn-default filterCollapseAll' type='button'>Collapse all</button>");
		writer.fwrite("<button class='btn btn-default timestamp' type='button'>Time off/on</button>");
		writer.fwrite("</td>");
		writer.fwrite("</tr>");
		writer.fwrite("</table>\n");
		writer.fwrite("<table class='table repLog table-bordered'>\n");
		//hide main table for improve speed
		writer.fwrite(
				  "<script type='text/javascript'>\n"
				+ "    document.getElementsByClassName('repLog')[0].style.display = 'none'"
				+ "</script>\n"
		);
		writer.fwrite("<tbody>");
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
		int passedStepsCount = super.passedStepsCount;
		int failedStepsCount = super.failedStepsCount;

		writer.fwrite("</tbody>");
		writer.fwrite("</table>");
		this.jsWriter.fwrite(
				  "var info = $('#tableInfo');\n"
				+ "$(info).find('#exec').html(%d)\n"
				+ "$(info).find('#pass').html(%d)\n"
				+ "$(info).find('#fail').html(%d)\n"
				+ "$(info).find('#startTime').html('%tF %tT')\n"
				+ "$(info).find('#finishTime').html('%tF %tT')\n"
				+ "$(info).find('#name').html('%s')\n"
				+ "$(info).find('#reportName').html('%s')\n",
				passedStepsCount != -1 ? passedStepsCount + failedStepsCount : passed + failed,
				passedStepsCount != -1 ? passedStepsCount : passed,
				failedStepsCount != -1 ? failedStepsCount : failed,
				startTime, startTime,
				finishTime, finishTime,
				Str.asString(name),
				reportName
		);

		//this is debug info about how long loading the page
		if (VersionInfo.isDevVersion())
		{
			this.jsWriter.fwrite(
					  "$(document).ready(function() {\n"
					+ "		var el = document.createElement('p');\n"
					+ "		el.innerText = \"Time until DOMready          : \" + (Date.now()-timerStart);\n"
					+ "		document.body.insertBefore(el, document.body.firstChild);\n"
					+ "	});\n"
					+ "	$(window).load(function() {\n"
					+ "		var el = document.createElement('p');\n"
					+ "		el.innerText = \"Time until everything loaded : \"+ (Date.now()-timerStart);\n"
					+ "		document.body.insertBefore(el, document.body.firstChild);\n"
					+ "	});\n");
		}
		this.jsWriter.fwrite(
				  "$(window).load(function() {\n"
				+ "    $('.repLog').show();\n"
				+ "});");
		this.jsWriter.fwrite("</script>");
		writer.fwrite(this.jsWriter.toString());
		writer.fwrite("</body>\n");
		writer.fwrite("</html>");
		this.jsWriter.close();
	}
	//endregion

	//region display executed matrix
	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
		writer.fwrite("<tr><td width='200'><a href='#' class='showSource'>Matrix <span class='caret'></span>  </a><td><span id='reportName'>%s</span>\n", matrixName);
		writer.fwrite("<tr class='matrixSource'><td colspan='2'>\n");
		this.jsWriter.fwrite("function copyToClipboard(elem) {\n"
				+ "var clone = $('#copy').clone();\n"
				+ "$('#copy').remove();\n"
				+ "    var targetId = '_hiddenCopyText_';\n"
				+ "    var origSelectionStart, origSelectionEnd;\n"
				+ "    target = document.getElementById(targetId);\n"
				+ "    if (!target) {\n"
				+ "var target = document.createElement(\"textarea\");\n"
				+ "target.style.position = 'absolute';\n"
				+ "target.style.left = '-9999px';\n"
				+ "target.style.top = '0';\n"
				+ "target.id = targetId;\n"
				+ "        document.body.appendChild(target);\n"
				+ "        }\n"
				+ "        target.textContent = elem.textContent;\n"
				+ "    \n"
				+ "    var currentFocus = document.activeElement;\n"
				+ "    target.focus();\n"
				+ "    target.setSelectionRange(0, target.value.length);\n"
				+ "    var succeed;\n"
				+ "    try {\n"
				+ "     succeed = document.execCommand(\"copy\");\n"
				+ "    } catch(e) {\n"
				+ "        succeed = false;\n"
				+ "    }\n"
				+ "    if (currentFocus && typeof currentFocus.focus === \"function\") {\n"
				+ "        currentFocus.focus();\n"
				+ "    }\n"
				+ "    target.textContent = \"\";\n"
				+ "    $('pre').prepend(clone); \n"
				+ "    return succeed;\n"
				+ "  }");
		writer.fwrite("<pre id='matrixSource'>");
		writer.fwrite("<button id=\"copy\" onclick=\"copyToClipboard(document.getElementById('matrixSource'))\" class='btn btn-default copyMatrix'>Copy</button>\n");
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
		writer.fwrite("</pre>\n");
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
		writer.fwrite("%s\n", line);
	}

	//endregion

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
	{
		String itemId = Str.asString(item.getId());

		//region display header

		String collect = item.getComments().stream().map(CommentString::toString).collect(Collectors.joining("<br>\n"));
		if (!collect.isEmpty())
		{
			writer.fwrite(
					  "<tr class='comment'><td colspan='%s'>\n"
					+ "%s"
					+ "</td></tr>\n"
					,this.columnCount, collect
			);
		}
		writer.fwrite("<tr id='tr_%s'>", id);
		writer.fwrite("<td class='timestamp'>%s</td>", DateTime.current().str());
		writer.fwrite(
				  "<th scope='row'>\n"
				+ "  <a href='javascript:void(0)' source='%s'>%03d</a>\n"
				+ "</th>\n",
				item.getSource().getName(), item.getNumber()); // TODO change to getSource()
		writer.fwrite("<td>%s</td>", itemId);
		writer.fwrite("<td><a href='javascript:void(0)' class='showBody'>%s</a></td>", item.getItemName());
		writer.fwrite("<td id='hs_%s'> </td>", id);
		writer.fwrite("<td id='time_%s'> </td>", id);
		writer.fwrite("<td id='src_%s'> </td>", id);
		writer.fwrite("</tr>");
		//endregion

		writer.fwrite("<tr>");
		writer.fwrite("<td colspan='%s' class='parTd'>", this.columnCount);
		writer.fwrite("<table class='table table-bordered innerTable'>");
		writer.fwrite("<tbody>");
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
		Result result = item.getResult() == null ? Result.NotExecuted : item.getResult().getResult();
		String styleClass = result.isFail() ? "danger" : "success";
		writer.fwrite("</tbody>");
		writer.fwrite("</table>");

		this.jsWriter.fwrite("var owner = $('#tr_%s');\n", id);
		this.jsWriter.fwrite("$(owner).addClass('%s')\n", styleClass);
		this.jsWriter.fwrite("$(owner).find('#hs_%s').html('<strong class=\"text-%s\">%s</strong>')\n", id, styleClass, result);
		this.jsWriter.fwrite("$(owner).find('#time_%s').html('%s ms');\n", id, time <= 1 ? "< 1" : time);
		if (screenshot != null)
		{
			String link = decorateLink(screenshot.getDescription(), getImageDir() + "/" + screenshot.getName(getReportDir()));
			this.jsWriter.fwrite("$(owner).find('#src_%s').html('%s');\n", id, link);
		}

		writer.fwrite("</td>");
		writer.fwrite("</tr>");
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException
	{
		if (beforeTestcase != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s' >\n",
					beforeTestcase);
		}
		writer.fwrite("<span class='tableTitle'>%s</span><br>",
				this.postProcess(title));

		switch (reportMode)
		{
			case AsEmbeddedImage:
				writer.fwrite("<img src='data:image/jpeg;base64,%s' class='img'/><br>", embedded);
				break;

			case AsImage:
				writer.fwrite("<img src='%s' class='img'/><br>", fileName);
				break;

			case AsLink:
				writer.fwrite("<a href=" + fileName+ ">Image</a><br>");
				break;

			default:
				break;
		}

		if (beforeTestcase != null)
		{
			writer.fwrite("</div>\n");
		}
	}

	@Override
	protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title) throws IOException
	{

	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		if (labelId == null)
		{
			writer.fwrite(postProcess(string));
		}
		else
		{
			writer.fwrite(
					  "<tr>\n"
					+ "  <td colspan='%s'>\n"
					+ "     <span class='label' id='%s'>%s</span>\n"
					+ "  </td>\n"
					+ "</tr>\n"
					, this.columnCount, labelId, string);
		}
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite("<tr>\n");
		writer.fwrite("<td>\n");
		if (table.getBeforeTestcase() != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s' >\n",table.getBeforeTestcase());
		}
		writer.fwrite("<span>%s</span>", this.postProcess(tableTitle));
		if (table.isBordered())
		{
			writer.fwrite("<table width='100%%' class='table table-bordered'>\n");
		}
		else
		{
			writer.fwrite("<table width='100%%' class='table'>\n");
		}

		//region display headers
		writer.fwrite("<thead>\n");
		for (int i = 0; i < columns.length; i++)
		{
			int percent = (percents == null || percents.length <= i) ? 0 : percents[i];
			if (percent <= 0)
			{
				writer.fwrite("<th>%s</th>", columns[i]);
			}
			else
			{
				writer.fwrite("<th width='%d%%'>%s</th>", percent, columns[i]);
			}
		}
		writer.fwrite("</thead>\n");
		//endregion

		writer.fwrite("<tbody>\n");

	}

	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
		{
			writer.fwrite("<tr>");
			int count = 0;
			for (Object obj : value)
			{
				String string = ReportHelper.objToString(obj, count >= quotes);
				String[] split = string.split("\n");
				StringBuilder sb = new StringBuilder();
				for (String str : split)
				{
					str = str.replace("\t","&nbsp;&nbsp;&nbsp;&nbsp;");
					sb.append(str).append("<br>");
				}
				writer.fwrite("<td class='tdMax'>%s</td>", sb.toString());
				count++;
			}
			writer.fwrite("</tr>");
			writer.fwrite("\n");
		}
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("</tbody>\n");
		writer.fwrite("</table>\n");
		if (table.getBeforeTestcase() != null)
		{
			writer.fwrite("</div>\n");
		}
		writer.fwrite("</td>");
		writer.fwrite("</tr>");
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		if (beforeTestCase != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s' >\n",
					beforeTestCase);
		}
		writer.fwrite("<span class='tableTitle'>%s</span><br>",
				this.postProcess(title));

		chartBuilder.report(writer, ++chartCount);

		if (beforeTestCase != null)
		{
			writer.fwrite("</div>\n");
		}

	}
}
