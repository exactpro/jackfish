////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

public class HTMLReportBuilder extends ReportBuilder 
{
	private static Integer diagramCount = 0;
	private static final String reportExt = ".html";
	private static final DateFormat dateTimeFormatter = new SimpleDateFormat("yyyyMMdd_HHmmss_");
	
	public HTMLReportBuilder(String outputPath, File matrix, Date currentTime) throws IOException
	{
		super(outputPath, matrix, currentTime);
	}

	@Override
	protected String postProcess(String result)
	{
		return super.postProcess(result);
//		return super.postProcess(HTMLhelper.htmlescape(result));
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		return String.format("<span class=\"%s\">%s</span>", style, value);
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
		writer.fwrite(String.format("<span id=\"TESTCASE_%s\"/>", mark));
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


	@Override
	protected void reportHeader(ReportWriter writer, Matrix context, Date date) throws IOException
	{
		writer.fwrite(
				"<html>\n" +
				"<head>\n" +
				"<title>Report</title>\n" +
				"<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n");

		writer.fwrite(
				"<script type='text/javascript'>\n" +
				"<!--\n");
		writer.include(getClass().getResourceAsStream("jquery-1.8.3.min.js"));
		writer.include(getClass().getResourceAsStream("reports.js"));
		writer.fwrite(
				"-->\n" +
				"</script>\n");

		writer.fwrite("<script>\n");
		writer.include(getClass().getResourceAsStream("d3.min.js"));
		writer.fwrite("</script>\n");
		writer.fwrite("<script>\n");
		writer.include(getClass().getResourceAsStream("histogram.js"));
		writer.fwrite("</script>\n");

		writer.fwrite(
				"<style>\n" +
				"<!--\n");
		writer.include(getClass().getResourceAsStream("style.css"));
		writer.fwrite(
				"-->\n" +
				"</style>\n");
		
		writer.fwrite(
				"</head>\n" +
				"<body>\n" +
				"<h1>EXECUTION REPORT</h1>\n" +
				"<table border='0' cellspacing='5'>\n"); 

		writer.fwrite("<tr><td><span id='name'></span>\n");
		writer.fwrite("<tr><td width='200'><h0>Version <td>%s</h0>\n", VersionInfo.getVersion());
		writer.fwrite("<tr><td>Start time: <td><span>%tF %tT</span>\n", date, date);
		writer.fwrite("<tr><td>Finish time: <td><span id='finishTime'>Calculating...</span>\n");
	}

	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
		writer.fwrite("<tr><td width='200'><a href='#' class='showSource'>Matrix:  </a><td>%s\n", matrixName);
		writer.fwrite("<tr class='matrixSource'><td colspan='2'>\n");
		writer.fwrite("<button onclick=\"copyToClipboard(document.getElementsByTagName('pre')[0].innerHTML)\">Copy matrix</button>\n");
		writer.fwrite("<script>\n");
		writer.fwrite("function copyToClipboard(text) {\n" +
				"	var w = document.createElement('textArea');\n" +
				"	w.value = text;\n" +
				"	w.setSelectionRange(0, text.length);\n" +
				"	//w.style.width = 0;\n" +
				"	//w.style.height = 0;\n" +
				"	document.body.appendChild(w);\n" +
				"	w.setSelectionRange(0, text.length);\n" +
				"	var q = document.execCommand('copy');\n" +
				"	document.body.removeChild(w);\n" +
				"  }");
		writer.fwrite("</script>\n");
		writer.fwrite("<pre>\n");
	}
	
	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
		writer.fwrite("%s\n", line);
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
		writer.fwrite("</pre>\n");
	}
	
	@Override
	protected void reportHeaderTotal(ReportWriter writer, Matrix context, Date date) throws IOException
	{
		writer.fwrite("<tr><td><a href='#' class='filterTotal'> Executed: </a><td> <span id='exec'>0</span>\n");
		writer.fwrite("<tr><td><a href='#' class='filterPassed'>Passed:   </a><td> <span id='pass'>0</span>\n");
		writer.fwrite("<tr><td><a href='#' class='filterFailed'>Failed:   </a><td> <span id='fail'>0</span>\n");
		writer.fwrite("<tr><td><a href='#' class='filterExpandAllFailed'>Show all failed</a><td>\n");
		writer.fwrite("<tr><td><a href='#' class='filterCollapseAll'>Collapse all</a><td>\n");
		writer.fwrite("</table>\n");
	}

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
	{
        String itemId = item.getId();

        if (itemId == null)
		{
            itemId = "";
        }					

		writer.fwrite(	
				"<div class='tree' id='%s'>\n",
				id);

		writer.fwrite(
				"<table border='0' cellspacing='0' width='50%%' >\n ");

		for (CommentString comment : item.getComments())
		{
			writer.fwrite(
					"<tr><td><td>%s</tr>\n",
					"" + comment); 
		}
		writer.fwrite("</table>\n");

		writer.fwrite(
				"<table border='0' cellspacing='0' width='50%%' >\n " +
				"<tr>" +
				"<td width='40px'>[%03d]" +
				"<td width='100px'><span class='Identity'>%s</span>" +
				"<td><a href='javascript:void(0)' class='showBody'>%s:</a>" +
				"<td width='200px'><span id='hs_%s'>Loading...</span>"+
				"<td width='100px'><span class='Time'>Time:</span>\n" +
				"<td class='ExecutionTime'><span id='time_%s'></span>\n",
				item.getNumber(),
				itemId,
				item.getItemName(),
				id,
				id );

		writer.fwrite(
				"</table>\n"); 

		
		writer.fwrite(
				"<div class='body'>\n");
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title) throws IOException
	{
		writer.fwrite(
				"<span class='tableTitle'>%s</span><br>",
				this.postProcess(title));
		
		writer.fwrite("<img src='%s' class='img'/><br>", fileName);
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
			writer.fwrite("<a href='javascript:void(0)' class='label' id='%s'>%s</a><br>", labelId, string);
		}
	}
	
	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time) throws IOException
	{
		Result result = item.getResult() == null ? Result.NotExecuted : item.getResult().getResult();
		
		writer.fwrite(
				"</div>\n");

		writer.fwrite("<script type='text/javascript'>\n" +
			    "<!--\n" );

		writer.fwrite("document.getElementById('hs_%s').innerHTML = '<span class=%s>%S</span>';\n", 
			    id,
			    result,
			    result );
		writer.fwrite("document.getElementById('time_%s').innerHTML = '<span>%s ms</span>';\n",
				id,
				time <= 1 ? "< 1" : time);

		writer.fwrite("document.getElementById('%s').title = '%s';\n", 
			    id,
			    result );

		writer.fwrite("-->\n" +
			    "</script>\n");
		
		writer.fwrite("</div>\n");
	}

	@Override
	protected void reportFooter(ReportWriter writer, MatrixItem item, Date date, String name) throws IOException
	{
		writer.fwrite("<script type='text/javascript'>\n" +
		    "<!--\n" +
		    "document.getElementById('exec').innerHTML = '<span> %d </span>'\n" + 
		    "document.getElementById('pass').innerHTML = '<span> %d </span>'\n" +
		    "document.getElementById('fail').innerHTML = '<span> %d </span>'\n" +
		    "document.getElementById('finishTime').innerHTML = '<span>%tF %tT</span>'\n" + 
		    "document.getElementById('name').innerHTML = '<span>%s</span>'\n" + 
		    "-->\n" +
		    "</script>\n",
		    	item.count(Result.Passed) + item.count(Result.Failed),
		    	item.count(Result.Passed),
		    	item.count(Result.Failed),
		    	date, date, 
		    	(name == null ? "" : name) 
				);
		
		writer.fwrite("</body>\n");
		writer.fwrite("</html>");
	}
	

	@Override
	protected void tableHeader(ReportWriter writer, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite(
				"<span class='tableTitle'>%s</span><br>",
				this.postProcess(tableTitle));

        writer.fwrite(
        		"<table width='100%%' border='1' bordercolor='#000000' cellpadding='3' cellspacing='0'>\n" +
        		"<tr style='font-weight: bold;'>\n");

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
	protected void tableRow(ReportWriter writer, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
			writer.fwrite("<tr>");
			int count = 0;
			for (Object obj : value)
			{
//				writer.fwrite("<td>%s", postProcess(ReportHelper.objToString(obj, count >= quotes)));
				//TODO workaround
				writer.fwrite("<td>%s", ReportHelper.objToString(obj, count >= quotes));
				count++;
			}
            writer.fwrite("\n");
        }
	}

	@Override
	protected void tableFooter(ReportWriter writer) throws IOException
	{
        writer.fwrite("</table>\n");
	}

	@Override
	protected void histogram(ReportWriter writer, String title, int intervalCount, int interval, List<Long> copyDate) throws IOException
	{
		Integer id = ++diagramCount;
		writer.fwrite("<div id=\"container" + id + "\" class='container' style=\"height:300px\">\n" + "</div>");
		writer.fwrite("<script>createHistogram([%s],%s,%s,%s,%s);</script>\n", copyDate.stream().map(String::valueOf).collect(Collectors.joining(",")), Collections.max(copyDate), interval, intervalCount, id);
		writer.fwrite("<ul>\n<li id=\"hstTimeRange" + id + "\">Range : </li>\n<li id=\"hstTimeCount" + id + "\">Count :</li>");
	}

}
