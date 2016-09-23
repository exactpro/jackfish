////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.IOException;
import java.util.Date;
import java.util.List;

public class ContextHelpBuilder extends ReportBuilder
{
    public ContextHelpBuilder(Date currentTime) throws IOException
    {
        super(null, null, currentTime);
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
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
		return null;
	}

	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
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
                "<style>\n" +
                "<!--\n");
        writer.include(getClass().getResourceAsStream("style.css"));
        writer.fwrite(
                "-->\n" +
                "</style>\n");


        writer.fwrite(
                "</head>\n" +
                "<body>\n" );
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
    }

    
    @Override
    protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
    {
        String name = item.getClass().getSimpleName();
    	if (item instanceof ActionItem)
    	{
    		name = item.getItemName();
    	}

        writer.fwrite("<h2>%s</h2>\n",
                name);
    }

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title) throws IOException
	{
		
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
	}
	
	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time) throws IOException
	{
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
	protected void histogram(ReportWriter writer, String title, int intervalCount, int interval, List<Long> copyDate)
	{

	}
}
