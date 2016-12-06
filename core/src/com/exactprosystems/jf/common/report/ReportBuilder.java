////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.CharArrayReader;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.sql.rowset.serial.SerialBlob;
import javax.sql.rowset.serial.SerialException;

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
	
	public final String getImageDir()
	{
		return this.imageDir;
	}

    public final Object reportAsArchieve() throws Exception
    {
        List<String> list = new ArrayList<>();
        list.add(this.reportName);
        File dir = new File(this.reportDir);
        if (dir.exists() && dir.isDirectory())
        {
            Arrays.stream(dir.list()).forEach(a -> list.add(this.reportDir + File.separator + a));;
        }
        
        ByteArrayOutputStream outputStream = null;

        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             ZipOutputStream zos = new ZipOutputStream(baos))
        {
            for (String filename : list)
            {
                Path path = Paths.get(filename);
                byte[] data = Files.readAllBytes(path);
                ZipEntry entry = new ZipEntry(filename);
                entry.setSize(data.length);
                zos.putNextEntry(entry);
                zos.write(data);
                zos.closeEntry();
            }
            outputStream = baos;
        }
        
        Blob blob = new SerialBlob(outputStream.toByteArray());
        return blob;
    }

	public final void reportSwitch(boolean on)
	{
		this.reportIsOn = on;
	}

	@Deprecated
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
		logger.trace(String.format("outImage(%s, %s, %s)", item, fileName, title));
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


		String fullName = writer.fileName();
		String postSuffix = this.name == null ? "" : " " + this.name;
		String replacement = (matrix.getRoot().count(Result.Failed) > 0 ? failed : passed) + postSuffix;
		if (fullName != null)
        {
			this.reportName = fullName.replace(suffix, replacement);
		}

		reportFooter(writer, matrix.getRoot(), new Date(), this.name, this.reportName);
		writer.close();

		if (fullName != null)
		{
			Files.move(Paths.get(fullName), Paths.get(fullName.replace(suffix, replacement)));
		}
	}

	public void reportChart(String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		logger.trace("reportChar");
		reportChart(this.writer, title, beforeTestCase, chartBuilder);
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

	protected abstract String decorateStyle(String value, String style);

	protected abstract String decorateLink(String name, String link);

	protected abstract String decorateExpandingBlock(String name, String content);

	protected abstract String replaceMarker(String marker);
	
	protected abstract String generateReportName(String outputPath, String matrixName, String suffix, Date date) throws IOException;

	protected abstract String generateReportDir(String matrixName, Date date) throws IOException;

	protected abstract void putMark(ReportWriter writer, String mark) throws IOException;

	protected abstract void reportHeader(ReportWriter writer, Matrix context, Date date) throws IOException;

	protected abstract void reportMatrixHeader(ReportWriter writer, String matrix) throws IOException;

	protected abstract void reportMatrixRow(ReportWriter writer, int count, String line) throws IOException;

	protected abstract void reportMatrixFooter(ReportWriter writer) throws IOException;
	
	protected abstract void reportHeaderTotal(ReportWriter writer, Matrix context, Date date) throws IOException;

	protected abstract void reportFooter(ReportWriter writer, MatrixItem entry, Date date, String name, String reportName) throws IOException;

	protected abstract void reportItemHeader(ReportWriter writer, MatrixItem entry, Integer id) throws IOException;
	
	protected abstract void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException;

	protected abstract void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String title) throws IOException;

	protected abstract void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time) throws IOException;
	
	protected abstract void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException;

	protected abstract void tableRow(ReportWriter writer, ReportTable table, int quotes, Object ... value) throws IOException;

	protected abstract void tableFooter(ReportWriter writer, ReportTable table) throws IOException;
	
	protected abstract void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException;


	
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
