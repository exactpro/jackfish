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

package com.exactprosystems.jf.common.report;

import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Storable;
import com.exactprosystems.jf.charts.ChartBuilder;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixRoot;
import com.exactprosystems.jf.functions.Content;
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
	private static final Logger logger = Logger.getLogger(ReportBuilder.class);

	int failedStepsCount = -1;
	int passedStepsCount = -1;

	private boolean reportIsOn;
	private String                          name       = null;
	private String                          reportName = null;
	private String                          imageDir   = null;
	private String                          reportDir  = null;
	private ReportWriter                    writer     = null;
	private Stack<Integer>                  uniques    = new Stack<>();
	private Integer                         uniqCount  = 0;
	private Map<Integer, List<ReportTable>> reportData = new HashMap<>();

	public enum ImageReportMode
	{
		AsLink, AsImage, AsEmbeddedImage;
	}

	public static final String SUFFIX = "_RUNNING";
	public static final String PASSED = "_PASSED";
	public static final String FAILED = "_FAILED";
	public static final String OM     = "{{";
	public static final String CM     = "}}";
	
	public ReportBuilder()
	{
		this.reportIsOn = true;
	}
	
	public ReportBuilder(String outputPath, String matrixName, Date currentTime) throws IOException
	{
		this.reportIsOn = true;
		
		if(outputPath != null && matrixName != null)
		{
			this.reportName = this.generateReportName(outputPath, matrixName, SUFFIX, currentTime);
			File file = new File(this.reportName);
			File parent = file.getParentFile();
			if (parent != null)
			{
				parent.mkdirs();
			}

			this.imageDir = this.generateReportDir(matrixName, currentTime);
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
			String[] dirList = dir.list();
			if (dirList != null)
			{
				Arrays.stream(dirList).forEach(a -> list.add(this.reportDir + File.separator + a));
			}
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
		return this.getClass().getSimpleName() + "{" + getReportName() + ":" + hashCode() + "}";
	}

	public final void init(ReportWriter writer)
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

	public final void putMark(String str) 
	{
		try
		{
			this.putMark(this.writer, str);
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public final ReportTable addExplicitTable(String title, String beforeTestcase, boolean decoraded, boolean bordered, int[] widths, String... columns)
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
		this.reportHeader(this.writer, startTime, version);
		if (matrixBuffer != null)
		{
			this.reportMatrix(this.writer, new BufferedReader(new CharArrayReader(matrixBuffer)));
		}
		this.reportHeaderTotal(this.writer, startTime);
	}
	
	public final void itemStarted(MatrixItem matrixItem)
	{
		Integer newUniq = this.generateNewUnique();
		this.uniques.push(newUniq);
		this.reportData.put(newUniq, new ArrayList<>());

		try
		{
			if (this.reportIsOn && !(matrixItem instanceof MatrixRoot))
			{
				this.reportItemHeader(this.writer, matrixItem, newUniq);
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
				this.outAllTables(this.reportData.get(uniq), writer);
			}
			this.reportData.get(uniq).clear();
		} 
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public final void outImage(MatrixItem item, String beforeTestcase, String fileName, byte[] data, String title, int scale, ImageReportMode reportMode)
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

			this.reportImage(this.writer, item, beforeTestcase, this.imageDir + File.separator + fileName, embedded, postProcess(title), scale, reportMode);
		}
		catch (IOException e)
		{
			logger.error(e.getMessage(), e);
		}
	}

	public final void outContent(MatrixItem item, String beforeTestcase, Content content, String title)
	{
		try
		{
			if (this.reportIsOn)
			{
				title = this.postProcess(title);
				this.reportContent(this.writer, item, beforeTestcase, content, title);
			}
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
			string = this.postProcess(string);
			if (labelId == null)
			{
				if (this.reportIsOn)
				{
					this.reportItemLine(this.writer, item, beforeTestcase, string, null);
				}
			}
			else
			{
				Integer uniq = this.uniques.peek();
				if (this.reportIsOn)
				{
					this.reportItemLine(this.writer, item, beforeTestcase, string, "" + uniq + "_" + labelId);
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
			this.outAllTables(this.reportData.get(uniq), this.writer);
			if (this.reportIsOn && !(matrixItem instanceof MatrixRoot))
			{
				this.reportItemFooter(this.writer, matrixItem, uniq, time, screenshot);
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
		String fullName = this.writer.fileName();
		String postSuffix = this.name == null ? "" : " " + this.name;
		String replacement = (failed > 0 ? FAILED : PASSED) + postSuffix;
		if (fullName != null)
        {
			this.reportName = fullName.replace(SUFFIX, replacement);
		}

		this.reportFooter(this.writer, failed, passed, startTime == null ? new Date() : startTime, finishTime == null ? new Date() : finishTime, this.name, this.reportName);
		this.writer.close();

		if (fullName != null)
		{
			Files.move(Paths.get(fullName), Paths.get(fullName.replace(SUFFIX, replacement)));
		}
	}

	public void reportChart(String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException
	{
		this.reportChart(this.writer, title, beforeTestCase, chartBuilder);
	}

	public void steps(int failed, int passed)
	{
		this.failedStepsCount = failed;
		this.passedStepsCount = passed;
	}

	protected String postProcess(String source)
	{
		if (source == null)
		{
			return null;
		}
		if (this.getMarker() != null)
		{
			return this.getMarker().process(source);
		}

        String reg = "((\\{\\{[1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-])|([1|2|3|4|5|$|#|@|\\^|`|_|*|/|&|=|-]\\}\\}))";

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

	protected Marker getMarker()
	{
		return null;
	}

	public String decorateStyle(Object value, String style)
	{
		if (value == null)
		{
			return "";
		}
		return this.decorateStyle(value.toString(), style);
	}

	public String decorateLink(Object value, String link)
	{
		if (value == null)
		{
			return "";
		}
		return this.decorateLink(value.toString(), link);
	}

	public String decorateExpandingBlock(Object value, String content)
	{
		if (value == null)
		{
			return "";
		}
		return this.decorateExpandingBlock(value.toString(), content);
	}

	public String decorateGroupCell(Object value, int level, boolean isNode)
	{
		if (value == null)
		{
			return "";
		}
		return this.decorateGroupCell(value.toString(), level, isNode);
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

	protected abstract void reportContent(ReportWriter writer, MatrixItem item, String beforeTestcase, Content content, String title) throws IOException;

	protected abstract void reportItemLine(ReportWriter writer, MatrixItem item, String beforeTestcase, String string, String labelId) throws IOException;

	protected abstract void reportImage(ReportWriter writer, MatrixItem item, String beforeTestcase, String fileName, String embedded, String title, int scale, ImageReportMode reportMode) throws IOException;

	protected abstract void reportItemFooter(ReportWriter writer, MatrixItem entry, Integer id, long time, ImageWrapper screenshot) throws IOException;

	protected abstract void tableHeader(ReportWriter writer, ReportTable table, String tableTitle, String[] columns, int[] percents) throws IOException;

	protected abstract void tableRow(ReportWriter writer, ReportTable table, int quotes, Object... value) throws IOException;

	protected abstract void tableFooter(ReportWriter writer, ReportTable table) throws IOException;

	protected abstract void reportChart(ReportWriter writer, String title, String beforeTestCase, ChartBuilder chartBuilder) throws IOException;


	private void reportMatrix(ReportWriter writer, BufferedReader reader)
	{
		if (reader == null)
		{
			return;
		}
		try
		{
			this.reportMatrixHeader(writer, this.reportName);

			String line;
			int count = 1;
			while ((line = reader.readLine()) != null)
			{
				this.reportMatrixRow(writer, count, line);
				count++;
			}
			reader.close();

			this.reportMatrixFooter(writer);
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
							columns[i] = this.postProcess(columns[i]);
						}
						this.tableHeader(writer, table, this.postProcess(table.getTitle()), columns, widths);
					}
					else
					{
						this.tableHeader(writer, table, table.getTitle(), columns, widths);
					}

					for (Object[] data : table.getData())
					{
						if (table.isDecorated())
						{
							for (int i = 0; i < data.length; i++)
							{
								data[i] = this.postProcess(data[i] == null ? null : data[i].toString());
							}
						}

						this.tableRow(writer, table, data.length, data);
					}

					this.tableFooter(writer, table);
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
}
