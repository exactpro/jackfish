////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.report;

import org.apache.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class StringWriter extends ReportWriter
{
	private StringBuilder builder;
	protected static final Logger logger = Logger.getLogger(StringWriter.class);

	public StringWriter()
	{
		super();
		this.builder = new StringBuilder();
	}

	@Override
	public ReportWriter fwrite(String fmt, Object... args)
	{
		this.builder.append(String.format(fmt, args));
		return this;
	}

	@Override
	public ReportWriter fwrite(String str)
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
	public void close()
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
}