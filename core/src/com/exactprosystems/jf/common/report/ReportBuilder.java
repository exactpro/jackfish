////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.CharArrayReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ReportBuilder 
{
	public final static String suffix 	= "_RUNNING";
	public final static String passed 	= "_PASSED";
	public final static String failed 	= "_FAILED";
	public final static String OM		= "{{";
	public final static String CM		= "}}";
	
	public ReportBuilder(String outputPath, File matrix, Date currentTime) throws IOException
	{
		logger.trace(String.format("ReportBuilder(%s, %s)", outputPath, matrix));
		this.reportIsOn = true;
		
		if(outputPath != null && matrix != null)
		{
			this.reportName = generateReportName(outputPath, matrix.getName(), suffix, currentTime);
			File file = new File(this.reportName);
			File parent = file.getParentFile();
			if (parent != null)
			{
				parent.mkdirs();
			}

			this.imageDir = generateReportDir(matrix.getName(), currentTime);
			this.reportDir = outputPath + File.separator + this.imageDir; 
		}
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
		logger.trace(String.format("putMark(%s)", str));
		putMark(this.writer, str);
	}

	public final ReportTable addTable(String title, String beforeTestcase, boolean decoraded, int quotedSince, int[] widths, String ... columns)
	{
		Integer uniq = this.uniques.peek();
		logger.trace(String.format("addTable(%s) current = %s", title, uniq));
		
		ReportTable info = new ReportTable(title, beforeTestcase, decoraded, quotedSince, widths, columns);
		
		this.reportData.get(uniq).add(info); // TODO deal with it
		
		return info;
	}

	public final void reportStarted(Matrix matrix) throws Exception 
	{
		logger.trace(String.format("reportStarted(%s)", matrix));
		Date startTime = new Date();

		this.reportData.clear();
		reportHeader(this.writer, matrix, startTime);
		char[] matrixBuffer = matrix.getMatrixBuffer();
		reportMatrix(this.writer, matrixBuffer == null ? null : new BufferedReader(new CharArrayReader(matrixBuffer)));
		reportHeaderTotal(this.writer, matrix, startTime);
	}
	
	public final void itemStarted(MatrixItem matrixItem)
	{
		Integer newUniq = generateNewUnique();
		this.uniques.push(newUniq);
		
		logger.trace(String.format("itemStarted(%s) current = %s", matrixItem.getItemName(), newUniq));

		this.reportData.put(newUniq, new ArrayList<ReportTable>());

		try
		{
			if (this.reportIsOn || matrixItem instanceof TestCase)
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
			logger.trace(String.format("itemIntermediate(%s) current = %s", matrixItem.getItemName(), uniq));
			 
			if (this.reportIsOn || matrixItem instanceof TestCase)
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
	
	public final void outImage(MatrixItem item, String beforeTestcase, String fileName, String title)
	{
		logger.trace(String.format("outImage(%s, %s, %s)", item.getItemName(), fileName, title));
		try
		{
			File dir = new File(this.reportDir);
			if (!dir.exists())
			{
				dir.mkdir();
			}
			
			reportImage(this.writer, item, beforeTestcase, this.imageDir + File.separator + fileName, postProcess(title));
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
	
	public final void itemFinished(MatrixItem matrixItem, long time)
	{
		try
		{
			Integer uniq = this.uniques.peek();
			logger.trace(String.format("itemFinished(%s) current = %s", matrixItem.getItemName(), uniq));
			 
			if (this.reportIsOn || matrixItem instanceof TestCase)
			{
				outAllTables(this.reportData.get(uniq), writer);
				reportItemFooter(this.writer, matrixItem, uniq, time);
			}
			this.uniques.pop();
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}
	
	public final void reportFinished(Matrix matrix) throws Exception 
	{
		logger.trace(String.format("reportFinished(%s)", matrix));

		reportFooter(writer, matrix.getRoot(), new Date(), this.name);
		writer.close();

		String fullName = writer.fileName();
        if (fullName != null)
        {
        	String postSuffix = this.name == null ? "" : " " + this.name;
        	
            if (matrix.getRoot().count(Result.Failed) > 0)
            {
                Files.move(Paths.get(fullName), Paths.get(fullName.replace(suffix, failed + postSuffix)));
				this.reportName = fullName.replace(suffix, failed + postSuffix);
            }
            else
            {
                Files.move(Paths.get(fullName), Paths.get(fullName.replace(suffix, passed + postSuffix)));
				this.reportName = fullName.replace(suffix, passed + postSuffix);
			}
        }
	}

	public final void reportHistogram(String title, int intervalCount, int interval, List<Long> copyDate) throws IOException
	{
		logger.trace("Report histogram");
		histogram(this.writer, title, intervalCount, interval, copyDate);
	}
	

	protected String postProcess(String source)
	{
		if (source == null)
		{
			return null;
		}
		
		String reg = "((\\{\\{[1|2|3|4|$|#|@|`|_])|([1|2|3|4|$|#|@|`|_]\\}\\}))";

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

	public String decorateLink(String name, String link)
	{
		return "";
	}
	
	protected abstract String decorateStyle(String value, String style);

	protected abstract String replaceMarker(String marker);
	
	protected abstract String generateReportName(String outputPath, String matrixName, String suffix, Date date) throws IOException;

	protected abstract String generateReportDir(String matrixName, Date date) throws IOException;

	protected abstract void putMark(ReportWriter writer, String mark) throws IOException;

	protected abstract void reportHeader(ReportWriter writer, Matrix context, Date date) throws IOException;

	protected abstract void reportMatrixHeader(ReportWriter writer, String matrix) throws IOException;

	protected abstract void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException;

	protected abstract void reportMatrixFooter(ReportWriter writer) throws IOException;
	
	protected abstract void reportHeaderTotal(ReportWriter writer, Matrix context, Date date) throws IOException;

	protected abstract void reportFooter(ReportWriter writer, MatrixItem entry, Date date, String name) throws IOException;

	protected abstract void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id) throws IOException;
	
	protected abstract void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException;

	protected abstract void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title) throws IOException;

	protected abstract void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time) throws IOException;
	
	protected abstract void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException;

	protected abstract void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException;

	protected abstract void tableFooter(ReportWriter writer, ReportTable table) throws IOException;
	
	protected abstract void histogram(ReportWriter writer, String title, int intervalCount, int interval, List<Long> copyDate) throws IOException;


	
	private void reportMatrix(ReportWriter writer, BufferedReader reader) throws IOException
	{
		logger.trace(String.format("reportMatrix(%s)", writer));

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
				reportMatrixRow(writer, count, postProcess(line));
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
		logger.trace(String.format("outAllTables(%s)", list));
		if (list != null)
    	{
    		for (ReportTable table : list)
    		{
    			if (table.getData() != null)
    			{
        			String[] columns = table.getColumns();
    				if (table.isDecorated())
    				{
	    				for (int i = 0; i < columns.length; i++)
	    				{
	    					columns[i] = postProcess(columns[i]);
	    				}
						tableHeader(writer, table, postProcess(table.getTitle()), columns, null);
    				}
    				else
    				{
						tableHeader(writer, table, table.getTitle(), columns, null);
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
