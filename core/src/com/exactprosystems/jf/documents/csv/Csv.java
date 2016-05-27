////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.csv;

import com.exactprosystems.jf.documents.AbstractDocument;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.functions.Table;

import java.io.Reader;

@DocumentInfo(newName = "Csv", extentioin = "csv", description = "CSV")
public class Csv extends AbstractDocument
{
	public Csv(String fileName, Configuration config)
	{
		super(fileName, config);

		this.table = new Table(new String[][] 
				{ 
					new String[] { "<none>" }, 
					new String[] { "" 		}, 
				}, null);
	}

	// ==============================================================================================================================
	// AbstractDocument
	// ==============================================================================================================================
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);

		this.table = new Table(reader, tableDelimiter, null);
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
		this.table.save(fileName, tableDelimiter, false, false);
	}

	// ------------------------------------------------------------------------------------------------------------------
	// interface Mutable
	// ------------------------------------------------------------------------------------------------------------------
	@Override
	public boolean isChanged()
	{
		return this.table.isChanged();
	}

	@Override
	public void saved()
	{
		super.saved();
		this.table.saved();
	}

	public void setDelimiter(char delimiter)
	{
		this.tableDelimiter = delimiter;
	}

	protected Table			table;

	protected char			tableDelimiter	= ';';
}
