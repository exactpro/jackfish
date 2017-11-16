////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
    public ReportWriter newline() throws IOException
    {
        this.writer.write("\n");
        return this;
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