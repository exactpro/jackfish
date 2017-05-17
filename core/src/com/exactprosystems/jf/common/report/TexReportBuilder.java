////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import java.io.*;
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
		return String.format("\\hyperlink{%s}{%s}", name.trim(), link.trim());
	}

	@Override
	protected String decorateExpandingBlock(String name, String content)
	{
		return name;
	}

    @Override
    protected String decorateGroupCell(String content, int level, boolean isNode)
    {
        return content;
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
            // header 1 (min level)
            case OM + "1": return "";
            case "1" + CM: return "";  
    
            // header 2
            case OM + "2": return "";
            case "2" + CM: return "";  
    
            // header 3
            case OM + "3": return "";
            case "3" + CM: return "";  
    
            // header 4 (max level)
            case OM + "4": return "";
            case "4" + CM: return "";  
    
            // style for identifiers
            case OM + "$": return "";     
            case "$" + CM: return "";       
    
            // style for code
            case OM + "#": return "";    
            case "#" + CM: return "";  
            
            // style for references
            case OM + "@": return "";
            case "@" + CM: return "";
    
            // paragraph
            case OM + "`": return "";
            case "`" + CM: return "\\newline";
    
            // underscored
            case OM + "_": return "";
            case "_" + CM: return "";
    
            // bolder
            case OM + "*": return "\\bold{";
            case "*" + CM: return "}";

            // italic
            case OM + "/": return "\\textit{";
            case "/" + CM: return "}";
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
	    return matrixName;
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
		writer.fwrite("\\end{document}");
	}
	//endregion

	//region display executed matrix
	@Override
	protected void reportMatrixHeader(ReportWriter writer, String matrixName) throws IOException
	{
	    // nothing to do
	}

	@Override
	protected void reportMatrixFooter(ReportWriter writer) throws IOException
	{
        // nothing to do
	}

	@Override
	protected void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException
	{
        // nothing to do
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

		writer.fwrite("\\subsection{%s}\n", item.getItemName().trim());
//        addBookmarks(writer, item.getItemName().trim());
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title, Boolean asLink) throws IOException
	{
		//TODO check center
        writer.fwrite("\\includegraphics[width=0.3\\textwidth]{%s}", fileName);
        if (!Str.IsNullOrEmpty(title))
        {
			writer.fwrite("\\caption{%s}\n", this.postProcess(title));
		}
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(postProcess(string) + "\n");
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite(" \n \\begin{table}[h] \n");
		if (!Str.IsNullOrEmpty(tableTitle))
		{
			writer.fwrite("\\caption{%s}\n", this.postProcess(tableTitle));
		}
		writer.fwrite("\\begin{center} \n");
		StringBuilder sb = new StringBuilder();
		sb.append("|");
		for (String c : columns)
		{
			sb.append("c|");
		}
		writer.fwrite("\\begin{tabular}{" + sb.toString() + "} \n");
		writer.fwrite("\\hline \n");

	}
	
	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
            if (value.length == 2){
                writer.fwrite("\\multicolumn{}{|p{0.4\\linewidth}|}{\\raggedright %s} & \\multicolumn{}{p{0.6\\linewidth}|}{\\raggedright %s}\\\\",
                        replaceMarker(ReportHelper.objToString(value[0], false)),
                        replaceMarker(ReportHelper.objToString(value[1], false)));
            }

            /*for (int i = 0; i < value.length ; i++)
        	{
        		if (i != value.length-1)
        		{
					writer.fwrite("%s & ", replaceMarker(ReportHelper.objToString(value[i], false)));
				} else
				{
					writer.fwrite("%s \\\\ ", replaceMarker(ReportHelper.objToString(value[i], false)));
				}
			}*/
            writer.fwrite("\n");
        }
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("\\hline \n");
		writer.fwrite("\\end{tabular} \n");
		writer.fwrite("\\end{center} \n");
        writer.fwrite("\\end{table} \n");
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		chartBuilder.report(writer, ++chartCount);
	}

//	private void addBookmarks(ReportWriter writer, String  uniqueName) throws IOException
//	{
//		writer.fwrite("\\hypertarget{%s}{}",  uniqueName);
//	}

}
