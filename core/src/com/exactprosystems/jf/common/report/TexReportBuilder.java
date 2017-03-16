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
import com.exactprosystems.jf.common.rtfhelp.Help;
import com.exactprosystems.jf.documents.matrix.parser.Parser;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemAttribute;
import com.exactprosystems.jf.documents.matrix.parser.items.TempItem;

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

	private static Help help = new Help();

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

            case OM + "!": return "";
            case "!" + CM: return "";

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
		deleteDocument(this.getReportDir());
	    writer.include(getClass().getResourceAsStream("tex1.txt"));
	}

	@Override
	protected void reportHeaderTotal(ReportWriter writer, Date date) throws IOException
	{
		addDocumentation(writer, help.introduction());
		addDocumentation(writer, help.panel());
		addDocumentation(writer, help.mvel());
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

		writer.fwrite("\\subsection{%s}\n", item.getItemName().trim());
        addBookmarks(writer, item.getItemName().trim());
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
        if (!Str.IsNullOrEmpty(title)){
			writer.fwrite("\\caption{%s}\n", this.postProcess(title));
		}
	}

	@Override
	protected void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException
	{
		writer.fwrite(postProcess(string) +  "\\newline \n");
	}

	@Override
	protected void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException
	{
		writer.fwrite(" \n \\begin{table} \n");
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
        	for (int i = 0; i < value.length -1; i++)
        	{
        		if (i != value.length-1)
        		{
					writer.fwrite("%s\\\\", ReportHelper.objToString(value[i], false));
				} else
				{
					writer.fwrite("%s&", ReportHelper.objToString(value[i], false));
				}
			}
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

	private String replaceChars (String s)
	{
		return s.replace("(?U)[\\pP\\s]", "").trim()
				.replace("{{&", "").replace("&}}", "")  //font2
				.replace("{{*", "").replace("*}}", "")  //bolder
				.replace("{{=", "").replace("=}}", "")  //table
				.replace("{{-", "").replace("-}}", "")  //row
				.replace("{{+", "").replace("+}}", "")  //cell
				.replace("{{$", "").replace("$}}", "")  //italic
				.replace("{{#", "").replace("#}}", "")  //code
				.replace("{{!", "").replace("!}}", "")  //header
				.replace("{{@", "").replace("@}}", ""); //link
	}

	private void addBookmarks(ReportWriter writer, String  uniqueName) throws IOException
	{
		writer.fwrite("\\hypertarget{%s}{}",  uniqueName);
	}

	private void addDocumentation(ReportWriter writer, InputStream is) throws IOException
	{
		BufferedReader br = new BufferedReader(new InputStreamReader(is));
		StringBuilder sb = new StringBuilder();
		for(String line; (line = br.readLine()) != null; )
		{
			if (line.contains("{{!") && line.contains("!}}")){
				sb.append("\\section{").append(replaceChars(line)).append("}");
			}
			else if (line.contains("{{#") && line.contains("#}}"))
			{
				sb.append("\\textit{").append(replaceChars(line)).append("}");
			}
			else if (line.equals("PutIntroPictureHere"))
			{
				writer.fwrite(sb.toString());
				sb.setLength(0);
				//todo
			}
			else if (line.contains("${"))
			{
				System.out.println(line);
				sb.append(replaceChars(line));
			}
			else
			{
				sb.append(replaceChars(line));
			}
			sb.append("\\newline \n");
		}
		if (sb.length() > 0)
		{
			writer.fwrite(sb.toString());
		}
	}

	private void deleteDocument(String path) {
		try {
			File file = new File(path);

			if (file.delete()) {
				//gratz!
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	//todo
	public void helpCreate(ReportBuilder report) throws Exception {
		report.reportStarted(null,"");
		makeItemHelp(report);
		report.reportFinished(0,0,new Date(),new Date());

		/*TexReportBuilder report = (TexReportBuilder) new TexReportFactory().createReportBuilder("/home/alexander.kruglov/Documents/shared folder VM", "new.txt", new Date());
		report.helpCreate(report);*/
	}

	private void makeItemHelp(ReportBuilder report) throws IllegalAccessException, InstantiationException
	{
		MatrixItem tmp = null;
		for (Class<?> clazz : Parser.knownItems)
		{
			MatrixItemAttribute attribute = clazz.getAnnotation(MatrixItemAttribute.class);
			if (attribute == null)
			{
				return;
			}

			if (!attribute.real() || clazz.equals(ActionItem.class) || clazz.equals(TempItem.class))
			{
				continue;
			}

			tmp = (MatrixItem) clazz.newInstance();
			report.itemStarted(tmp);
			report.itemIntermediate(tmp);
			report.outLine(tmp, "", attribute.description(), null);

			if (attribute.seeAlsoClass().length > 0)
			{
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < attribute.seeAlsoClass().length -1; i++)
				{
					String l = attribute.seeAlsoClass()[i].getSimpleName();
					sb.append(decorateLink(l, l));
					if (i != attribute.seeAlsoClass().length -1){
						sb.append(" , ");
					}
				}
				report.outLine(tmp, "", sb.toString(), null);
			}

			if (!attribute.examples().equals(""))
			{
				//report.outLine(tmp, "", "Examples:", null);
				//report.outLine(tmp, "", note("Examples", attribute.examples()), null);
			}
			report.itemFinished(tmp, 0, null);
		}
	}
}
