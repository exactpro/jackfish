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

package com.exactprosystems.jf.common.documentation;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.common.report.ReportWriter;
import com.exactprosystems.jf.documents.matrix.parser.items.*;
import com.exactprosystems.jf.functions.Content;

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
    
    private static final double TEXT_WIDTH = 150; // mm

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
		Pattern patt = Pattern.compile("([^\\\\]_)");
		Matcher m = patt.matcher(result);
		StringBuffer sb = new StringBuffer(result.length());
		while (m.find())
		{
			String text = m.group(1);
			text = text.replace("_", "\\\\_");
			m.appendReplacement(sb, text);
		}
		m.appendTail(sb);
		return super.postProcess(sb.toString());
	}

	@Override
	protected String decorateStyle(String value, String style)
	{
		String rgbColor;
		switch (style){
			case "BLACK": rgbColor = "black";
				break;
			case "BLUE": rgbColor = "blue";
				break;
			case "CYAN": rgbColor = "cyan";
				break;
			case "DARK_GRAY": rgbColor = "darkgray";
				break;
			case "GRAY": rgbColor = "gray";
				break;
			case "GREEN": rgbColor = "green";
				break;
			case "LIGHT_GRAY": rgbColor = "lightgray";
				break;
			case "MAGENTA": rgbColor = "magenta";
				break;
			case "ORANGE": rgbColor = "orange";
				break;
			case "PINK": rgbColor = "pink";
				break;
			case "RED": rgbColor = "red";
				break;
			case "WHITE": rgbColor = "white";
				break;
			case "Failed": rgbColor = "red";
				break;
			default: rgbColor = "black";
		}

		return String.format("\\textcolor{%s}{%s}", rgbColor, value);
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
			// header 1
			case OM + "1": return "\\\\section{ ";
			case "1" + CM: return "}";

			// header 2
			case OM + "2": return "\\\\subsection{ ";
			case "2" + CM: return "}";

			// header 3
			case OM + "3": return "\\\\subsubsection{ ";
			case "3" + CM: return "}";

            // header 4
            case OM + "4": return "\\\\normalsize{";
            case "4" + CM: return "}";

			// header 5
			case OM + "5": return "\\\\large{";
			case "5" + CM: return "}";

			// style for identifiers
            case OM + "$": return "{\\\\color{codecolor} \\\\verb+";
            case "$" + CM: return "+}";

			// http://tostudents.ru/2010/01/07/overfull-i-underfull-perepolnennye-i-razrezhennye-stroki/
            // style for code
            case OM + "#": return "\\\\begingroup\n" +
								"    \\\\fontsize{12pt}{10pt}\\\\selectfont\\\\color{codecolor}\n" +
								"    \\\\begin{verbatim}  ";
            case "#" + CM: return "  \\\\end{verbatim}\n" +
									"\\\\endgroup";
            
            // style for references
            case OM + "@": return "\\\\superhyperlink{";
            case "@" + CM: return "}";
    
            // text 90 degrees rotated
            case OM + "^": return "\\\\rotatebox{90}{";
            case "^" + CM: return "}";

            // paragraph
            case OM + "`": return "";
            case "`" + CM: return "\\\\newline{}";
    
            // new page
            case OM + "&": return "";
            case "&" + CM: return "\\\\newpage \\\\pagestyle{allpages}";

            // underscored
            case OM + "_": return "\\\\underline{";
            case "_" + CM: return "}";
    
            // bolder
            case OM + "*": return "\\\\textbf{";
            case "*" + CM: return "}";

            // italic
            case OM + "/": return "\\\\textit{";
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
		writer.fwrite(String.format("\\hypertarget{%s} \\newline ", mark));
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

		try (InputStream isHeader = getClass().getResourceAsStream("Header.png"))
		{
			File header = new File(this.getReportDir() + File.separator + "header.png");
			Files.deleteIfExists(header.toPath());
			Files.copy(isHeader, header.toPath());
		}

		try (InputStream recIS = getClass().getResourceAsStream("Square.png"))
		{
			File rec = new File(this.getReportDir() + File.separator + "Square.png");
			Files.deleteIfExists(rec.toPath());
			Files.copy(recIS, rec.toPath());
		}
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
	}

	@Override
	protected void reportItemFooter(ReportWriter writer, MatrixItem item, Integer id, long time, ImageWrapper screenshot) throws IOException
	{
	}

	@Override
	protected void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException
	{
        writer.fwrite("\\includegraphics[width=%f\\textwidth]{%s}", scale < 0 ? 1.0 : scale / 100.0, fileName.replace(File.separator, "")).newline();
		writer.fwrite("\\newline").newline();
	}

    @Override
    protected void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content,
            String title) throws IOException
    {
        writer.fwrite("{{\\hypersetup{linkcolor=black}\\tableofcontents}}").newline();
    }
	
	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(string).newline();
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		if (!Str.IsNullOrEmpty(tableTitle))
		{
			writer.fwrite("\\newline \\caption{%s}", tableTitle).newline();
		}
		String delimiter = table.isBordered() ? "|" : "";
	    writer.fwrite("\\begin{center}").newline();
		String tab = IntStream.range(0, columns.length)
		        .mapToObj(i -> i)
		        .map(o -> (percents.length <= o ? "l" : "p{" + (percents[o] * TEXT_WIDTH / 100) + "mm}"))
		        .collect(Collectors.joining(delimiter));

		if (table.isBordered()){
			writer.fwrite(String.format("\\begin{longtable}{|%s|} \\hline", tab)).newline();
		}
		else {
			writer.fwrite(String.format("\\begin{longtable}{%s}", tab)).newline();
		}

		tableRow(writer, table, 0, columns);
	}
	
	@Override
	protected void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException
	{
		if (value != null)
        {
		    String s = Arrays.stream(value).map(o -> Objects.toString(o)).reduce((s1, s2) -> s1 + "&" + s2).orElse("");
			writer.fwrite(s).fwrite("\\\\");
		    if (table.isBordered()) {
                writer.fwrite(" \\hline");
            }
		    writer.newline();
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
}
