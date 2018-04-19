/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.common.report;

import java.io.*;

public class FileReportWriter extends ReportWriter
{
	private Writer writer;
	private String fileName;

	public FileReportWriter(String fileName) throws IOException 
	{
		this.fileName = fileName;
		this.writer = new BufferedWriter(new FileWriter(fileName));
	}

	@Override
	public ReportWriter fwrite(String fmt, Object... args) throws IOException
	{
		this.writer.write(String.format(fmt, args));
		return this;
	}
	
	@Override
	public ReportWriter fwrite(String str) throws IOException 
	{
		this.writer.write(str);
		return this;
	}
	
	@Override
	public void close() throws IOException
	{
		this.writer.close();
	}

	@Override
	public void include(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			this.writer.write(line);
			super.newline();
		}
		
		in.close();
	}
	
	@Override
	public String fileName()
	{
		return this.fileName;
	}

}