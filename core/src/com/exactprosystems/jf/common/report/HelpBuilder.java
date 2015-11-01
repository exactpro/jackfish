////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ControlKind;
import com.exactprosystems.jf.api.app.OperationKind;
import com.exactprosystems.jf.common.ControlsAttributes;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Result;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.version.VersionInfo;
import com.exactprosystems.jf.common.xml.control.AbstractControl;

import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HelpBuilder extends ReportBuilder
{
    public HelpBuilder(Date currentTime) throws IOException
    {
        super(null, null, currentTime);
    }

    @Override
    protected String generateReportName(String outputPath, String matrixName, String suffix, Date date)
    {
        return "";
    }

	@Override
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
		return null;
	}

    @Override
    protected void reportHeader(ReportWriter writer, Matrix context, Date date) throws IOException
    {
        writer.fwrite(
                "<html>\n" +
                "<head>\n" +
                "<title>Help</title>\n" +
                "<meta http-equiv='Content-Type' content='text/html; charset=utf-8'>\n");

        writer.fwrite(
                "<script type='text/javascript'>\n" +
                "<!--\n");
        writer.include(getClass().getResourceAsStream("jquery-1.8.3.min.js"));
        writer.include(getClass().getResourceAsStream("reports.js"));
        writer.fwrite(
                "-->\n" +
                "</script>\n");

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
                "<h0>Version <td>%s</h0>\n",
                VersionInfo.getVersion());
    }

    @Override
    protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
    {
    }

    @Override
    protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
    {
    }

    @Override
    protected void reportMatrixFooter(ReportWriter writer) throws IOException
    {
    }


	@Override
    protected void reportHeaderTotal(ReportWriter writer, Matrix context, Date date) throws IOException
    {
        makeChapter(writer, "MVEL syntax", "mvel.html");
		makeAllControls(writer);
    }

	private void makeAllControls(ReportWriter writer) throws IOException
	{
		writer.fwrite("<div class='tree'>\n" +
				"<table border='0' cellspacing='0' width='50%%' > \n " +
				"<tr>" +
				"<td><a href='#' class='showBody'>All Controls:</a>" +
				"<td width='200px'>" +
				"<span class=PASSED></span>\n" +
				"</table>\n");
		writer.fwrite("<div class='tree'>");
		writer.fwrite("<table border='1px'>\n");
		writer.fwrite("<thead><tr>\n");
		writer.fwrite("<th>Control name</th>");
		for (OperationKind kind : OperationKind.values())
		{
			writer.fwrite("<th>%s</th>", kind.toString());
		}
		writer.fwrite("</tr></thead>");
		writer.fwrite("<tbody>");
		for (ControlKind k : ControlKind.values())
		{
			try
			{
				writer.fwrite("<tr>");
				Class<?> controlClass = Class.forName(AbstractControl.class.getPackage().getName() +"."+ k.getClazz());
				writer.fwrite("<td>%s</td>", controlClass.getSimpleName());
				ControlsAttributes annotation = controlClass.getAnnotation(ControlsAttributes.class);
				OperationKind defaultOperation = annotation.bindedClass().defaultOperation();
				for (OperationKind kind : OperationKind.values())
				{
					writer.fwrite("<td>");
					if (annotation.bindedClass().isAllowed(kind))
					{
						writer.fwrite("<label");
						if (kind == defaultOperation)
						{
							writer.fwrite(" class='defaultOperation'");
						}
						writer.fwrite(">+</label>");
					}
					writer.fwrite("</td>");
				}
				writer.fwrite("</tr>");
			}
			catch (Exception e)
			{
				e.printStackTrace();
			}
		}
		writer.fwrite("</tbody></table>");
		writer.fwrite("</div></div>");
	}

	private void makeChapter(ReportWriter writer, String chapterName, String fileName) throws IOException
    {
        writer.fwrite(
        		"<div class='tree'>\n" +
                "<table border='0' cellspacing='0' width='50%%' >\n " +
                "<tr>" +
                "<td><a href='#' class='showBody'>%s:</a>" +
                "<td width='200px'>" +
                "<span class=PASSED></span>\n",
                chapterName);

        writer.fwrite(
                "</table>\n");
    	
        writer.include(getClass().getResourceAsStream(fileName));
        
		writer.fwrite("</div>\n");
        
    }
    
    @Override
    protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
    {
        String name = item.getItemName();
        writer.fwrite(
                "<div class='tree' id='%s'>\n",
                id);

        writer.fwrite(
                "<table border='0' cellspacing='0' width='50%%' >\n " +
                "<tr>" +
                        "<td><a href='#' class='showBody'>%s:</a>" +
                        "<td width='200px'><span id='hs_%s'>Loading...</span>",
                name,
                id );

        writer.fwrite(
                "</table>\n");


        writer.fwrite(
                "<div class='body'>\n");
    }

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String fileName, String title) throws IOException
	{
	}


	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String string, String labelId) throws IOException
	{
		if (labelId == null)
		{
			writer.fwrite(string);
		}
		else
		{
			writer.fwrite("<a href='#' class='label' id='%s'>%s</a><br>", labelId, string);
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
		
		writer.fwrite("document.getElementById('hs_%s').innerHTML = '<span class=%s></span>';\n", 
			    id,
			    result);

        writer.fwrite("document.getElementById('time_%s').innerHTML = '<span>Execution time : %sms</span>';\n",
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
	protected String postProcess(String result)
	{
		return HTMLhelper.htmlescape(result);
	}
}
