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