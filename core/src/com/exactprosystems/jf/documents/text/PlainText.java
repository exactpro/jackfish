////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
        extension = "txt",
        description = "Plain text"
)
public class PlainText extends AbstractDocument
{
	protected MutableValue<String>		property;

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
		this.property.accept(read(reader));
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
		write(fileName);
		super.save(fileName);
		saved();
	}
	//endregion

	@Override
	public boolean isChanged()
	{
		return this.property.isChanged();
	}

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
			String line = null;
			while ((line = buffReader.readLine()) != null)
			{
				sb.append(line).append('\n');
			}
		}
		return sb.toString();
	}

	private void write(String fileName) throws IOException
	{
		try (Writer writer = CommonHelper.writerToFileName(fileName); 
		     BufferedWriter buffWriter = new BufferedWriter(writer))
		{
			for (String line : this.property.get().split("\n"))
			{
				buffWriter.write(line);
				buffWriter.newLine();
			}
		}
	}

	//endregion
}
