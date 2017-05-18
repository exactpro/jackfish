////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

class StringWriter extends  ReportWriter  
{
	public StringWriter() 
	{
		this.builder = new StringBuilder();
	}
	
    @Override
    public ReportWriter newline() throws IOException
    {
        this.builder.append('\n');
        return this;
    }

    @Override
	public ReportWriter fwrite(String fmt, Object... args) throws IOException 
	{
		this.builder.append(String.format(fmt, args));
		return this;
	}
	
	@Override
	public ReportWriter fwrite(String str) throws IOException 
	{
		this.builder.append(str);
		return this;
	}
	
	@Override
	public void include(InputStream in) throws IOException
	{
		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String line;
		while ((line = reader.readLine()) != null)
		{
			this.builder.append(line);
			this.builder.append("\n\r");
		}
		
		in.close();
	}
	
	@Override
	public void close() throws IOException
	{
	}
	
	@Override
	public String fileName()
	{
		return null;
	}
	
	@Override
	public String toString()
	{
		return this.builder.toString();
	}
	
	private StringBuilder builder;
	
	
	protected static final Logger logger = Logger.getLogger(StringWriter.class);
}