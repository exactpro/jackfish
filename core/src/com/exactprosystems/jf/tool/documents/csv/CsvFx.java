////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.documents.csv;

import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.io.Reader;

public class CsvFx extends Csv
{
	private TableDataProvider provider;

	public CsvFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		this.provider = new TableDataProvider(super.table, this::addCommand);
	}

	public DataProvider<String> getProvider()
	{
		return provider;
	}

	//region Abstract document
	@Override
	public void display() throws Exception
	{
		super.display();
		this.provider.fire();
	}

	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.provider.setTable(super.table);
	}

	@Override
	public boolean canClose() throws Exception
	{
		if (!super.canClose())
		{
			return false;
		}

		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(getNameProperty().get());
			if (desision == ButtonType.YES)
			{
				save(getNameProperty().get());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}

		return true;
	}
	//endregion
}
