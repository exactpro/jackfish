////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Writer;

public class FileReportWriter extends ReportWriter
{
	public FileReportWriter(String fileName) throws IOException 
	{
		this.fileName = fileName;
		this.writer = new BufferedWriter(new FileWriter(fileName));
	}
	
	@Override
	public void fwrite(String fmt, Object... args) throws IOException 
	{
		this.writer.write(String.format(fmt, args));
	}
	
	@Override
	public void fwrite(String str) throws IOException 
	{
		this.writer.write(str);
	}
	
	@Override
	public void close() throws IOException
	{
	    this.writer.flush();
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
			this.writer.write("\n\r");
		}
		
		in.close();
	}
	
	@Override
	public String fileName()
	{
		return this.fileName;
	}
	
	private Writer writer;

	private String fileName;
}