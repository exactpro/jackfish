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
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.stream.Collectors;

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
            case OM + "3": return "\\\\newpage";
            case "3" + CM: return "";  
    
            // header 4 (max level)
            case OM + "4": return "\\\\newpage";
            case "4" + CM: return "";  
    
            // style for identifiers
            case OM + "$": return "";     
            case "$" + CM: return "";       
    
            // style for code
            case OM + "#": return "\\\\begin{alltt}";    
            case "#" + CM: return "\\\\end{alltt}";  
            
            // style for references
            case OM + "@": return "";
            case "@" + CM: return "";
    
            // paragraph
            case OM + "`": return "";
            case "`" + CM: return "\\\\newline";
    
            // underscored
            case OM + "_": return "";
            case "_" + CM: return "";
    
            // bolder
            case OM + "*": return "\\\\textbf{";
            case "*" + CM: return "}";

            // italic
            case OM + "/": return "\\\\textit{";
            case "/" + CM: return "}";
            
            // table
            case OM + "=": return "\\\\begin{longtable}[h]{lp{0.7\\\\linewidth}} \\\\begin{tabular}{|p{0}|p{50pt}|p{150pt}|p{150pt}|p{100pt}|p{}|p{}|p{}|p{}|p{}|p{}|} \\\\hline";
            case "=" + CM: return "\\\\end{tabular} \\\\end{longtable}";

            // row table
            case OM + "-": return "";
            case "-" + CM: return "\\\\\\\\ \\\\hline";

            // cell of row table
            case OM + "+": return "&";
            case "+" + CM: return "";
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
	    writer.fwrite("\\begin{document}");
//        %% \\maketitle
//        %% \\newpage
//        %% \\tableofcontents
//        %% \\newpage
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
	    // nothing to do
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

//		writer.fwrite("\\subsection{%s}", itemId).newline();
//        addBookmarks(writer, item.getItemName().trim());
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title, Boolean asLink) throws IOException
	{
        writer.fwrite("\\includegraphics[width=0.3\\textwidth]{%s}", fileName).newline();
        if (!Str.IsNullOrEmpty(title))
        {
			writer.fwrite("\\caption{%s}", title).newline();
		}
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(string).newline();
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
	    System.err.println(">> " + Arrays.toString(percents));
	    
		writer.fwrite("\\begin{longtable}[h]{lp{0.7\\linewidth}}").newline();
		if (!Str.IsNullOrEmpty(tableTitle))
		{
			writer.fwrite("\\caption{%s} \\newline", tableTitle).newline();
		}
		String tab = Arrays.stream(columns).map(c -> "l").collect(Collectors.joining("|"));
		writer.fwrite("\\begin{tabular}{|%s|} \\hline", tab).newline();
		tableRow(writer, table, 0, columns);
	}
	
	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
		    String s = Arrays.stream(value).map(o -> Objects.toString(o)).reduce((s1, s2) -> s1 + "&" + s2).orElse("");
            writer.fwrite(s).fwrite("\\\\ \\hline").newline();
        }
	}

	@Override
	protected void tableFooter(ReportWriter writer, ReportTable table) throws IOException
	{
		writer.fwrite("\\end{tabular}").newline()
		    .fwrite("\\end{longtable}").newline();
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		chartBuilder.report(writer, ++chartCount);
	}
}
