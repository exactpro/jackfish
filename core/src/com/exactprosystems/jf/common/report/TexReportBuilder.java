////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import java.io.File;
import java.io.IOException;
import java.util.Date;

public class TexReportBuilder extends ReportBuilder 
{
    private static final long serialVersionUID = -6980809888694705058L;

    private static Integer chartCount = 0;
	
	public TexReportBuilder()
	{
		super();
	}

	public TexReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
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
		return value;
	}

	@Override
	protected String decorateLink(String name, String link)
	{
		String res = String.format("\\ref{%s}", name);
		return res;
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return name;
	}

	@Override
	protected String replaceMarker(String marker)
	{
        if (marker == null)
        {
            return "";
        }
        
        switch (marker)
        {
            case OM + "1": return "";
            case "1" + CM: return "";  

            case OM + "2": return "";
            case "2" + CM: return "";  

            case OM + "3": return "";
            case "3" + CM: return "";  

            case OM + "4": return "";
            case "4" + CM: return "";  

            case OM + "$": return "";     
            case "$" + CM: return "";       

            case OM + "#": return "";    
            case "#" + CM: return "";  
            
            case OM + "@": return "";
            case "@" + CM: return "";

            case OM + "`": return "";
            case "`" + CM: return "\n\n";

            case OM + "_": return "";
            case "_" + CM: return "";
        }
        return "";		
	}

	@Override
	protected String generateReportName(String outputPath, String matrixName, String suffix, Date date) 
	{
		return outputPath + File.separator + matrixName;
	}
	
	@Override
	protected void putMark(ReportWriter writer, String mark) throws IOException
	{
		writer.fwrite(String.format("\\label{%s}", mark));
	}

	@Override
	protected String generateReportDir(String matrixName, Date date) throws IOException
	{
	    return null;
	}

	//region Global report
	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
	    writer.include(getClass().getResourceAsStream("tex1.txt"));
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
	}

	@Override
	protected void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException
	{
	}
	//endregion

	//region display executed matrix
	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
	}

	//endregion

	@Override
	protected void reportItemHeader(ReportWriter writer, MatrixItem item, Integer id) throws IOException
	{
        String itemId = item.getId();

        if (itemId == null)
		{
            itemId = "";
        }

		writer.fwrite("%s\n", item.getItemName());
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title, Boolean asLink) throws IOException
	{
        writer.fwrite("\\includegraphics[width=0.3\\textwidth]{%s}", fileName);
	    writer.fwrite("\\caption{%s}\n", this.postProcess(title));
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(postProcess(string));
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite("\\begin{table} \n");
	}
	
	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
			for (Object obj : value)
			{
				writer.fwrite("%s", ReportHelper.objToString(obj, false));
			}
            writer.fwrite("\n");
        }
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
        writer.fwrite("\\end{table} \n");
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		chartBuilder.report(writer, ++chartCount);
	}
}
