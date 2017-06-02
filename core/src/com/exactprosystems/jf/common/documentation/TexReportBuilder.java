////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.common.report.ReportBuilder.ImageReportMode;
import com.exactprosystems.jf.documents.matrix.parser.items.*;

import java.io.*;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TexReportBuilder extends ReportBuilder
{
    private static final long serialVersionUID = -6980809888694705058L;
    
    private static final int TEXT_WIDTH = 150; // mm

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
		return replaceSymbols(super.postProcess(result));
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
	protected String replaceMarker(String marker) //https://www.sharelatex.com/learn/Sections_and_chapters
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
            case OM + "2": return "\\\\section{ ";
            case "2" + CM: return " }";
    
            // header 3
            case OM + "3": return "\\\\subsection{ ";
            case "3" + CM: return " }";
    
            // header 4 (max level)
            case OM + "4": return "\\\\newpage ";
            case "4" + CM: return "";  
    
            // style for identifiers
            case OM + "$": return "";     
            case "$" + CM: return "";

			// http://tostudents.ru/2010/01/07/overfull-i-underfull-perepolnennye-i-razrezhennye-stroki/
            // style for code
            case OM + "#": return "\\\\begingroup\n" +
								"    \\\\fontsize{12pt}{10pt}\\\\selectfont\n" +
								"    \\\\begin{verbatim}  ";
            case "#" + CM: return "  \\\\end{verbatim}\n" +
									"\\\\endgroup";
            
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

			case OM + "&": return "";
			case "&" + CM: return "";
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
	    return "";
	}

	//region Global report
	@Override
	protected void reportHeader(ReportWriter writer, Date date, String version) throws IOException
	{
	    writer.include(getClass().getResourceAsStream("tex1.txt"));

	    InputStream isFooter = getClass().getResourceAsStream("Footer.png");
	    File footer = new File(this.getReportDir() + File.separator + "footer.png");
		Files.deleteIfExists(footer.toPath());
		Files.copy(isFooter, footer.toPath());
		isFooter.close();

		InputStream isHeader = getClass().getResourceAsStream("Header.png");
		File header = new File(this.getReportDir() + File.separator + "header.png");
		Files.deleteIfExists(header.toPath());
		Files.copy(isHeader, header.toPath());
		isHeader.close();
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
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, ImageReportMode reportMode) throws IOException
	{
        writer.fwrite("\\includegraphics[width=0.9\\textwidth]{%s}", fileName.replace("/", "")).newline();
		writer.fwrite("\\newline").newline();
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(string).newline();
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
	    double constant = 0.9;

	    writer.fwrite("\\begin{center}").newline();
		if (!Str.IsNullOrEmpty(tableTitle))
		{
			writer.fwrite("\\caption{%s} \\newline", tableTitle).newline();
		}
		String tab = IntStream.range(0, columns.length)
		        .mapToObj(i -> i)
		        .map(o -> (percents.length <= o ? "l" : "p{" + (percents[o] * TEXT_WIDTH * constant/ 100) + "mm}"))
		        .collect(Collectors.joining("|"));

		writer.fwrite(String.format("\\begin{longtable}{|%s|} \\hline", tab)).newline();
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
		writer.fwrite("\\end{longtable}").newline()
			.fwrite("\\end{center}").newline();
	}

	@Override
	protected void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		chartBuilder.report(writer, ++chartCount);
	}

	private String replaceSymbols(String source)
	{
		String reg = "(&&|~|&|^|%|#|$)";
		Pattern patt = Pattern.compile(reg);
		Matcher m = patt.matcher(source);
		StringBuffer sb = new StringBuffer(source.length());
		while (m.find())
		{
			String text = m.group(1);
			switch (text)
			{
				case "&": text = text.replace("&", "\\\\&");
					break;
				case "&&": text = text.replace("&&", "\\\\&\\\\&");
					break;
				case "~": text = text.replace("~", "\\\\sim");
					break;
				case "^": text = text.replace("^", "\\\\wedge");
					break;
				case "%": text = text.replace("%", "\\\\%");
					break;
				case "#": text = text.replace("#", "\\\\#");
					break;
				case "$": text = text.replace("$", "\\\\textdollar");
					break;
				default:
			}
			m.appendReplacement(sb, text);
		}
		m.appendTail(sb);

		return sb.toString();

	}
}
