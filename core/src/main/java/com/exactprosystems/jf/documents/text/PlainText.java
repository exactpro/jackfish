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

package com.exactprosystems.jf.documents.text;

import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.matrix.parser.MutableValue;

import java.io.*;

@DocumentInfo(
        kind = DocumentKind.PLAIN_TEXT,
        newName = "Text", 
        extension = "txt,sql,jf,ini,properties,xml,html,htm,rtf,xhtml,xht,java,js,inf,conf,config,cfg,xsd,bat,sh,tex",
        description = "Plain text"
)
public class PlainText extends AbstractDocument
{
	/**
	 * The field, which contains text, which used on this class
	 */
	protected final MutableValue<String> property;

	public PlainText(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.property = new MutableValue<>("");
	}

	//region AbstractDocument
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.property.accept(this.read(reader));
		this.property.saved();
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		this.write(fileName);
		super.save(fileName);
		this.saved();
	}
	//endregion

	//region interface Mutable
	@Override
	public boolean isChanged()
	{
		return this.property.isChanged() || super.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.property.saved();
	}
	//endregion

	public MutableValue<String> getProperty()
	{
		return this.property;
	}

	//region private methods
	private String read(Reader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		try (BufferedReader buffReader = new BufferedReader(reader))
		{
			String line;
			while ((line = buffReader.readLine()) != null)
			{
				sb.append(line).append(System.lineSeparator());
			}
		}
		return sb.toString();
	}

	private void write(String fileName) throws IOException
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName);
			 BufferedWriter buffWriter = new BufferedWriter(writer))
		{
			for (String line : this.property.get().split(System.lineSeparator()))
			{
				buffWriter.write(line);
				buffWriter.newLine();
			}
		}
	}

	//endregion
}
