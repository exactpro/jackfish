////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Storable;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReportBuilder implements Storable
{
    private static final long serialVersionUID = -4301681183671789970L;

    public enum ImageReportMode
    {
        AsLink,
        AsImage,
        AsEmbeddedImage;
    }
    
    public final static String SUFFIX 	= "_RUNNING";
	public final static String PASSED 	= "_PASSED";
	public final static String FAILED 	= "_FAILED";
	public final static String OM		= "{{";
	public final static String CM		= "}}";
	
	public ReportBuilder()
	{
		this.reportIsOn = true;
	}
	
	public ReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		this.reportIsOn = true;
		
		if(outputPath != null && matrixName != null)
		{
			this.reportName = generateReportName(outputPath, matrixName, SUFFIX, currentTime);
			File file = new File(this.reportName);
			File parent = file.getParentFile();
			if (parent != null)
			{
				parent.mkdirs();
			}

			this.imageDir = generateReportDir(matrixName, currentTime);
			this.reportDir = outputPath + File.separator + this.imageDir; 
		}
	}
	
	@Override
	public String getName() 
	{
		if (this.reportName == null)
		{
			return "";
		}
		return new File(this.reportName).getName();
	}

	@Override
	public List<String> getFileList() 
	{
		List<String> list = new ArrayList<>();
		list.add(this.reportName);
		File dir = new File(this.reportDir);
		if (dir.exists() && dir.isDirectory()) 
		{
			Arrays.stream(dir.list()).forEach(a -> list.add(this.reportDir + File.separator + a));
		}
		return list;
	}

	@Override
	public byte[] getData(String file) throws IOException 
	{
		Path path = Paths.get(file);
		return Files.readAllBytes(path);
	}

	@Override
	public void addFile(String file, byte[] data) throws Exception 
	{
		File f = new File(file);
		f.getParentFile().mkdirs();

		if (file.endsWith(".html"))
		{
			this.reportName = file;
		}

		try (ByteArrayInputStream bais = new ByteArrayInputStream(data);
				FileOutputStream fos = new FileOutputStream(file) )
		{
			byte[] buffer = new byte[1024];
			int len;
			while ((len = bais.read(buffer)) > 0)
			{
				fos.write(buffer, 0, len);
			}
		}
	}

	@Override
    public String toString()
    {
        return getClass().getSimpleName() + "{" + getReportName() + ":" + hashCode() + "}";
    }

	public final void init(ReportWriter writer) throws IOException
	{
		this.writer = writer;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public final String getContent()
	{
		return this.writer.toString();
	}

	public final String getReportName()
	{
		return this.reportName;
	}
	
	public final String getReportDir()
	{
		return this.reportDir;
	}
	
	public final String getImageDir()
	{
		return this.imageDir;
	}

	public final void reportSwitch(boolean on)
	{
		this.reportIsOn = on;
	}

	public final boolean reportIsOn()
	{
		return this.reportIsOn;
	}

	public final void putMark(String str) throws Exception 
	{
		putMark(this.writer, str);
	}

    public final ReportTable addExplicitTable(String title, String beforeTestcase, boolean decoraded, boolean bordered, int[] widths, String ... columns)
    {
        Integer uniq = this.uniques.peek();
        ReportTable info = new ReportTable(title, beforeTestcase, decoraded, bordered, widths, columns);
        this.reportData.get(uniq).add(info);
        
        return info;
    }

	public final ReportTable addTable(String title, String beforeTestcase, boolean decoraded, boolean bordered, int[] widths, String ... columns)
	{
		Integer uniq = this.uniques.peek();
		ReportTable info = new ReportTable(title, beforeTestcase, decoraded, bordered, widths, columns);
		if (this.reportIsOn())
		{
		    this.reportData.get(uniq).add(info);
		}
		
		return info;
	}

	public final void reportStarted(char[] matrixBuffer, String version) throws Exception 
	{
		Date startTime = new Date();

		this.reportData.clear();
		reportHeader(this.writer, startTime, version);
		if (matrixBuffer != null)
		{
		    reportMatrix(this.writer, new BufferedReader(new CharArrayReader(matrixBuffer)));
		}
		reportHeaderTotal(this.writer, startTime);
	}
	
	public final void itemStarted(MatrixItem matrixItem)
	{
		Integer newUniq = generateNewUnique();
		this.uniques.push(newUniq);
		  
		this.reportData.put(newUniq, new ArrayList<ReportTable>());

		try
		{
			if (this.reportIsOn && !(matrixItem instanceof MatrixRoot))
			{
				reportItemHeader(this.writer, matrixItem, newUniq);
			}
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public final void itemIntermediate(MatrixItem matrixItem)
	{
		try
		{
			Integer uniq = this.uniques.peek();
			if (this.reportIsOn && !(matrixItem instanceof MatrixRoot))
			{
				outAllTables(this.reportData.get(uniq), writer);
			}
			this.reportData.get(uniq).clear();
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public final void outImage(MatrixItem item, String beforeTestcase, String fileName, byte[] data, String title, ImageReportMode reportMode)
	{
		try
		{
		    if (this.reportDir != null)
		    {
    			File dir = new File(this.reportDir); 
    			if (!dir.exists())
    			{
    				dir.mkdir();
    			}
		    }

            String embedded = "";
		    if (data != null)
		    {
    	        byte[] encoded = java.util.Base64.getEncoder().encode(data);
    	        embedded = new String(encoded);
		    }
			
			reportImage(this.writer, item, beforeTestcase, this.imageDir + File.separator + fileName, embedded, postProcess(title), reportMode);
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public final void outLine(MatrixItem item, String beforeTestcase, String string, Integer labelId)
	{
		try
		{
			string = postProcess(string);
			if (labelId == null)
			{
				if (this.reportIsOn)
				{
					reportItemLine(this.writer, item, beforeTestcase, string, null);
				}
			}
			else
			{
				Integer uniq = this.uniques.peek();
				if (this.reportIsOn)
				{
					reportItemLine(this.writer, item, beforeTestcase, string, "" + uniq + "_" + labelId);
				}
			}
		} 
		catch (IOException e)
		{ 
			logger.error(e.getMessage(), e);
		}
	}
	
	public final void itemFinished(MatrixItem matrixItem, long time, ImageWrapper screenshot)
	{
		try
		{
			Integer uniq = this.uniques.peek();
            outAllTables(this.reportData.get(uniq), writer);
			if (this.reportIsOn)
			{
				if (!(matrixItem instanceof MatrixRoot))
				{
				    reportItemFooter(this.writer, matrixItem, uniq, time, screenshot);
				}
			}
			this.uniques.pop();
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public final void reportFinished(int failed, int passed, Date startTime, Date finishTime) throws Exception 
	{
		String fullName = writer.fileName();
		String postSuffix = this.name == null ? "" : " " + this.name;
		String replacement = (failed > 0 ? FAILED : PASSED) + postSuffix;
		if (fullName != null)
        {
			this.reportName = fullName.replace(SUFFIX, replacement);
		}

		reportFooter(writer, failed, passed, startTime == null ? new Date() : startTime, 
				finishTime == null ? new Date() : finishTime, this.name, this.reportName);
		writer.close();

		if (fullName != null)
		{
			Files.move(Paths.get(fullName), Paths.get(fullName.replace(SUFFIX, replacement)));
		}
	}

	public void reportChart(String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		reportChart(this.writer, title, beforeTestCase, chartBuilder);
	}


	protected String postProcess(String source)
	{
		if (source == null)
		{
			return null;
		}
		
        String reg = "((\\{\\{[1|2|3|4|$|#|@|`|_|*|/|&|=|-])|([1|2|3|4|$|#|@|`|_|*|/|&|=|-]\\}\\}))";

		Pattern patt = Pattern.compile(reg);
		Matcher m = patt.matcher(source);
		StringBuffer sb = new StringBuffer(source.length());
		while (m.find())
		{
			String text = m.group(1);
			String replace = replaceMarker(text);
			m.appendReplacement(sb, replace);
		}
		m.appendTail(sb);
		
		return sb.toString();
	}
	
	public String decorateStyle(Object value, String style)
	{
		if (value == null)
		{
			return "";
		}
		return decorateStyle(value.toString(), style);
	}

	public String decorateLink(Object value, String link)
	{
		if (value == null)
		{
			return "";
		}
		return decorateLink(value.toString(), link);
	}

	public String decorateExpandingBlock(Object value, String content)
	{
		if (value == null)
		{
			return "";
		}
		return decorateExpandingBlock(value.toString(), content);
	}

    public String decorateGroupCell(Object value, int level, boolean isNode)
    {
        if (value == null)
        {
            return "";
        }
        return decorateGroupCell(value.toString(), level, isNode);
    }

	protected abstract String decorateStyle(String value, String style);

	protected abstract String decorateLink(String name, String link);

	protected abstract String decorateExpandingBlock(String name, String content);

    protected abstract String decorateGroupCell(String content, int level, boolean isNode);

    protected abstract String replaceMarker(String marker);
	
	protected abstract String generateReportName(String outputPath, String matrixName, String suffix, Date date) throws IOException;

	protected abstract String generateReportDir(String matrixName, Date date) throws IOException;

	protected abstract void putMark(ReportWriter writer, String mark) throws IOException;

	protected abstract void reportHeader(ReportWriter writer, Date date, String version) throws IOException;

	protected abstract void reportMatrixHeader(ReportWriter writer, String matrix) throws IOException;

	protected abstract void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException;

	protected abstract void reportMatrixFooter(ReportWriter writer) throws IOException;
	
	protected abstract void reportHeaderTotal(ReportWriter writer, Date date) throws IOException;

	protected abstract void reportFooter(ReportWriter writer, int failed, int passed, Date startTime, Date finishTime, String name, String reportName) throws IOException;

	protected abstract void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id) throws IOException;
	
	protected abstract void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException;

	protected abstract void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, ImageReportMode reportMode) throws IOException;

	protected abstract void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time, ImageWrapper screenshot) throws IOException;
	
	protected abstract void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException;

	protected abstract void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException;

	protected abstract void tableFooter(ReportWriter writer, ReportTable table) throws IOException;
	
	protected abstract void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException;


	
	private void reportMatrix(ReportWriter writer, BufferedReader reader) throws IOException
	{
        if (reader == null)
        {
            return;
        }
		try
	    {
			reportMatrixHeader(writer, this.reportName);

			BufferedReader src = reader;
			String line;
			int count = 1;
			while ((line = src.readLine()) != null)
			{
				reportMatrixRow(writer, count, line);
				count++;
			}
			src.close();

			reportMatrixFooter(writer);
	    }
	    catch (Exception e)
	    {
	    	logger.error(e.getMessage(), e);
	    }
	}

	private void outAllTables(List<ReportTable> list, ReportWriter writer) throws IOException
	{
		if (list != null)
    	{
    		for (ReportTable table : list)
    		{
    			if (table.getData() != null)
    			{
        			String[] columns = table.getColumns();
        			int[] widths = table.getWidths();
    				if (table.isDecorated())
    				{
	    				for (int i = 0; i < columns.length; i++)
	    				{
	    					columns[i] = postProcess(columns[i]);
	    				}
						tableHeader(writer, table, postProcess(table.getTitle()), columns, widths);
    				}
    				else
    				{
						tableHeader(writer, table, table.getTitle(), columns, widths);
    				}
    				
		
		        	for (Object[] data : table.getData())
		        	{
	    				if (table.isDecorated())
	    				{
		    				for (int i = 0; i < data.length; i++)
		    				{
		    					data[i] = postProcess(data[i] == null ? null : data[i].toString());
		    				}
	    				}
		        		
		        		tableRow(writer, table, data.length, data);
		        	}
		
		        	tableFooter(writer, table);
    			}
    		}
    		list.clear();
    	}
	}

	private Integer generateNewUnique()
	{
		synchronized (this)
		{
			return ++this.uniqCount;
		}
	}

	private boolean reportIsOn;
	
	private String name = null;
	
	private String reportName = null; 

	private String imageDir = null; 

	private String reportDir = null; 
	
	private ReportWriter writer = null;

	private Stack<Integer> uniques = new Stack<Integer>();
	
	private Integer uniqCount = 0;
	
	private Map<Integer, List<ReportTable>> reportData = new HashMap<Integer, List<ReportTable>>();


	protected static final Logger logger = Logger.getLogger(ReportBuilder.class);
}
