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
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Content;
import com.exactprosystems.jf.tool.Common;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Collectors;

public class HTMLReportBuilder extends ReportBuilder
{
	private static final long serialVersionUID = 8277698425881479782L;

	private static       Integer      chartCount        = 0;
	private static final String       reportExt         = ".html";
	private static final DateFormat   dateTimeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");
	private static final int          columnCount       = 7;
	private              StringWriter jsWriter          = new StringWriter();

	public HTMLReportBuilder()
	{
		super();
	}

	public HTMLReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		super(outputPath, matrixName, currentTime);
	}

	@Override
	protected Marker getMarker()
	{
		return new Marker.HTMLMaker(true);
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
		String res;
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
	protected String generateReportDir(String matrixName, Date date)
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
		this.jsWriter.fwrite("<script type='text/javascript'>").newline();

		writer.fwrite("<html>").newline()
				.fwrite("<head>").newline()
				.fwrite("<script type='text/javascript'>").newline()
				.fwrite("    var timerStart = Date.now();").newline()
				.fwrite("</script>").newline()
				.fwrite("<title>Report</title>").newline()
				.fwrite("<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>").newline();

		writer.fwrite("<style>").newline();
		writer.include(this.getClass().getResourceAsStream("style.css"));
		writer.newline().fwrite("</style>").newline();

		writer.fwrite("<script>").newline();
		writer.include(this.getClass().getResourceAsStream("jquery-1.8.3.min.js"));
		writer.include(this.getClass().getResourceAsStream("reports.js"));
		writer.include(this.getClass().getResourceAsStream("charts.js"));
		writer.include(this.getClass().getResourceAsStream("d3.min.js"));
		writer.newline().fwrite("</script>").newline();

		writer
				.fwrite("</head>").newline()
				.fwrite("<body>").newline()
				.fwrite("<h1>EXECUTION REPORT</h1>").newline()
				.fwrite("<table id='tableInfo' class='table'>").newline()

				.fwrite("<tr><td><span id='name'></span>").newline()
				.fwrite("<tr><td>Version <td>%s", Str.asString(version)).newline()
				.fwrite("<tr><td>Start time: <td><span id='startTime'>Calculating...</span>").newline()
				.fwrite("<tr><td>Finish time: <td><span id='finishTime'>Calculating...</span>").newline();
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
		writer.fwrite("<tr>").newline()
				.fwrite("<td colspan='2'>").newline()
				.fwrite("<button class='btn btn-info filterTotal' type='button'>Executed : <span id='exec' class='badge'>0</span></button>").newline()
				.fwrite("<button class='btn btn-success filterPassed' type='button'>Passed : <span id='pass' class='badge'>0</span></button>").newline()
				.fwrite("<button class='btn btn-danger filterFailed' type='button'>Failed : <span id='fail' class='badge'>0</span></button>").newline()
				.fwrite("<button class='btn btn-default filterExpandAllFailed' type='button'><span class='text-danger'>Expand all failed</span></button>").newline()
				.fwrite("<button class='btn btn-default filterCollapseAll' type='button'>Collapse all</button>").newline()
				.fwrite("<button class='btn btn-default timestamp' type='button'>Time off/on</button>").newline()
				.fwrite("</td>").newline()
				.fwrite("</tr>").newline()
				.fwrite("</table>").newline()
				.fwrite("<table class='table repLog table-bordered'>").newline();

		//hide main table for improve speed
		writer.fwrite("<script type='text/javascript'>").newline()
				.fwrite("    document.getElementsByClassName('repLog')[0].style.display = 'none'").newline()
				.fwrite("</script>").newline()
				.fwrite("<tbody>").newline();
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
		int passedStepsCount = super.passedStepsCount;
		int failedStepsCount = super.failedStepsCount;

		writer.fwrite("</tbody>").newline()
				.fwrite("</table>").newline();

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
			this.jsWriter
					.fwrite("$(document).ready(function() {").newline()
					.fwrite("		var el = document.createElement('p');").newline()
					.fwrite("		el.innerText = \"Time until DOMready          : \" + (Date.now()-timerStart);").newline()
					.fwrite("		document.body.insertBefore(el, document.body.firstChild);").newline()
					.fwrite("	});").newline()
					.fwrite("	$(window).load(function() {").newline()
					.fwrite("		var el = document.createElement('p');").newline()
					.fwrite("		el.innerText = \"Time until everything loaded : \"+ (Date.now()-timerStart);").newline()
					.fwrite("		document.body.insertBefore(el, document.body.firstChild);").newline()
					.fwrite("	});").newline();
		}
		this.jsWriter
				.fwrite("$(window).load(function() {").newline()
				.fwrite("    $('.repLog').show();").newline()
				.fwrite("});").newline()
				.fwrite("</script>").newline();

		writer.fwrite(this.jsWriter.toString())
				.fwrite("</body>").newline()
				.fwrite("</html>");

		this.jsWriter.close();
	}
	//endregion

	//region display executed matrix
	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
		writer.fwrite("<tr><td width='200'><a href='#' class='showSource'>Matrix <span class='caret'></span>  </a><td><span id='reportName'>%s</span>", matrixName).newline()
				.fwrite("<tr class='matrixSource'><td colspan='2'>").newline();

		this.jsWriter
				.fwrite("function copyToClipboard(elem) {").newline()
				.fwrite("    var clone = $('#copy').clone();").newline()
				.fwrite("    $('#copy').remove();").newline()
				.fwrite("    var targetId = '_hiddenCopyText_';").newline()
				.fwrite("    var origSelectionStart, origSelectionEnd;").newline()
				.fwrite("    target = document.getElementById(targetId);").newline()
				.fwrite("    if (!target) {").newline()
				.fwrite("        var target = document.createElement(\"textarea\");").newline()
				.fwrite("        target.style.position = 'absolute';").newline()
				.fwrite("        target.style.left = '-9999px';").newline()
				.fwrite("        target.style.top = '0';").newline()
				.fwrite("        target.id = targetId;").newline()
				.fwrite("        document.body.appendChild(target);").newline()
				.fwrite("    }").newline()
				.fwrite("    target.textContent = elem.textContent;").newline()
				.fwrite("    ").newline()
				.fwrite("    var currentFocus = document.activeElement;").newline()
				.fwrite("    target.focus();").newline()
				.fwrite("    target.setSelectionRange(0, target.value.length);").newline()
				.fwrite("    var succeed;").newline()
				.fwrite("    try {").newline()
				.fwrite("        succeed = document.execCommand(\"copy\");").newline()
				.fwrite("    } catch(e) {").newline()
				.fwrite("        succeed = false;").newline()
				.fwrite("    }").newline()
				.fwrite("    if (currentFocus && typeof currentFocus.focus === \"function\") {").newline()
				.fwrite("        currentFocus.focus();").newline()
				.fwrite("    }").newline()
				.fwrite("    target.textContent = \"\";").newline()
				.fwrite("    $('pre').prepend(clone); ").newline()
				.fwrite("    return succeed;").newline()
				.fwrite("}").newline();

		writer.fwrite("<pre id='matrixSource'>")
				.fwrite("<button id=\"copy\" onclick=\"copyToClipboard(document.getElementById('matrixSource'))\" class='btn btn-default copyMatrix'>Copy</button>\n");
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
		writer.fwrite("</pre>").newline();
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
		writer.fwrite(line).newline();
	}

	//endregion

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
	{
		String itemId = Str.asString(item.getId());

		//region display header

		String collect = item.getComments().stream()
				.map(MutableValue::toString)
				.collect(Collectors.joining("<br>\n"));

		if (!collect.isEmpty())
		{
			writer.fwrite("<tr class='comment'><td colspan='%s'>", columnCount).newline()
					.fwrite(collect).newline()
					.fwrite("</td></tr>").newline();
		}
		writer.fwrite("<tr id='tr_%s'>", id).newline()
				.fwrite("<td class='timestamp'>%s</td>", DateTime.current().str()).newline()
				.fwrite("<th scope='row'>").newline()
				.fwrite("    <a href='javascript:void(0)' source='%s'>%03d</a>",Common.getRelativePath(item.getSource().getNameProperty().get()), item.getNumber()).newline()
				.fwrite("</th>").newline()
				.fwrite("<td>%s</td>", itemId).newline()
				.fwrite("<td><a href='javascript:void(0)' class='showBody'>%s</a></td>", item.getItemName()).newline()
				.fwrite("<td id='hs_%s'> </td>", id).newline()
				.fwrite("<td id='time_%s'> </td>", id).newline()
				.fwrite("<td id='src_%s'> </td>", id).newline()
				.fwrite("</tr>").newline();
		//endregion

		writer.fwrite("<tr>").newline()
				.fwrite("<td colspan='%s' class='parTd'>", columnCount).newline()
				.fwrite("<table class='table table-bordered innerTable'>").newline()
				.fwrite("<tbody>").newline();
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
		Result result = item.getResult() == null ? Result.NotExecuted : item.getResult().getResult();
		String styleClass = result.isFail() ? "danger" : "success";
		writer.fwrite("</tbody>").newline()
				.fwrite("</table>").newline();

		this.jsWriter.fwrite("var owner = $('#tr_%s');", id).newline()
				.fwrite("$(owner).addClass('%s')", styleClass).newline()
				.fwrite("$(owner).find('#hs_%s').html('<strong class=\"text-%s\">%s</strong>')", id, styleClass, result).newline()
				.fwrite("$(owner).find('#time_%s').html('%s ms');", id, time <= 1 ? "< 1" : time).newline();

		if (screenshot != null)
		{
			String link = decorateLink(screenshot.getDescription(), getImageDir() + "/" + screenshot.getName(getReportDir()));
			this.jsWriter.fwrite("$(owner).find('#src_%s').html('%s');", id, link).newline();
		}

		writer.fwrite("</td>").newline()
				.fwrite("</tr>").newline();
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException
	{
		if (beforeTestcase != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s'>",beforeTestcase).newline();
		}
		writer.fwrite("<span class='tableTitle'>%s</span><br>", this.postProcess(title)).newline();

		switch (reportMode)
		{
			case AsEmbeddedImage:
				writer.fwrite("<img src='data:image/jpeg;base64,%s' class='img'/><br>", embedded).newline();
				break;

			case AsImage:
				writer.fwrite("<img src='%s' class='img'/><br>", fileName).newline();
				break;

			case AsLink:
				writer.fwrite("<a href=" + fileName+ ">Image</a><br>").newline();
				break;

			default:
				break;
		}

		if (beforeTestcase != null)
		{
			writer.fwrite("</div>").newline();
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
					, columnCount, labelId, string);
		}
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite("<tr>").newline()
				.fwrite("<td>").newline();
		if (table.getBeforeTestcase() != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s' >",table.getBeforeTestcase()).newline();
		}
		writer.fwrite("<span>%s</span>", this.postProcess(tableTitle)).newline();
		if (table.isBordered())
		{
			writer.fwrite("<table width='100%%' class='table table-bordered'>").newline();
		}
		else
		{
			writer.fwrite("<table width='100%%' class='table'>").newline();
		}

		//region display headers
		writer.fwrite("<thead>").newline();
		for (int i = 0; i < columns.length; i++)
		{
			int percent = (percents == null || percents.length <= i) ? 0 : percents[i];
			if (percent <= 0)
			{
				writer.fwrite("<th>%s</th>", columns[i]).newline();
			}
			else
			{
				writer.fwrite("<th width='%d%%'>%s</th>", percent, columns[i]).newline();
			}
		}
		writer.fwrite("</thead>").newline();
		//endregion

		writer.fwrite("<tbody>").newline();

	}

	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
		{
			writer.fwrite("<tr>").newline();
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
				writer.fwrite("<td class='tdMax'>%s</td>", sb.toString()).newline();
				count++;
			}
			writer.fwrite("</tr>").newline();
		}
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("</tbody>").newline()
				.fwrite("</table>").newline();
		if (table.getBeforeTestcase() != null)
		{
			writer.fwrite("</div>").newline();
		}
		writer.fwrite("</td>").newline()
				.fwrite("</tr>").newline();
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		if (beforeTestCase != null)
		{
			writer.fwrite("<div class='movable' data-moveto='%s' >",beforeTestCase).newline();
		}
		writer.fwrite("<span class='tableTitle'>%s</span><br>",this.postProcess(title)).newline();

		chartBuilder.report(writer, ++chartCount);

		if (beforeTestCase != null)
		{
			writer.fwrite("</div>").newline();
		}
	}
}
