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