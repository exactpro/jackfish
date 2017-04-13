////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.text;

import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.DocumentInfo;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.io.*;

@DocumentInfo(newName = "Text", extentioin = "txt", description = "Plain text")
public class PlainText extends AbstractDocument
{
	public PlainText(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.property = new SimpleStringProperty()
		{
			@Override
			public void set(String arg0)
			{
				super.set(arg0);
				changed = true;
			}
		};
	}

	// ==============================================================================================================================
	// AbstractDocument
	// ==============================================================================================================================
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.property.set(read(reader));
	}

	@Override
	public boolean canClose() throws Exception
	{
		return true;
	}

	@Override
	public void save(String fileName) throws Exception
	{
		super.save(fileName);
		write(fileName);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// interface Mutable
	// ------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		return this.changed;
	}

	@Override
	public void saved()
	{
		super.saved();
		this.changed = false;
	}

	// ------------------------------------------------------------------------------------------------------------------
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

	protected StringProperty			property;		// TODO we need our own string holder 

	protected boolean					changed				= false;
}
