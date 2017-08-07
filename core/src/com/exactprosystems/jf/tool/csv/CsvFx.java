////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.csv;

import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.csv.Csv;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.io.Reader;
import java.util.Optional;

public class CsvFx extends Csv
{
	public CsvFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
		
		this.provider = new TableDataProvider(super.table, this::addCommand);
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		
		initController();
		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayTable(this.provider);
	}

	@Override
	public void create() throws Exception
	{
		super.create();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);
		this.provider = new TableDataProvider(super.table, this::addCommand);
	}

    @Override
    public boolean canClose()  throws Exception
    {
		if (!super.canClose())
		{
			return false;
		}

		if (isChanged())
		{
			ButtonType desision = DialogsHelper.showSaveFileDialog(this.getName());
			if (desision == ButtonType.YES)
			{
				save(getName());
			}
			if (desision == ButtonType.CANCEL)
			{
				return false;
			}
		}
		
		return true;
    }

    @Override
    public void save(String fileName) throws Exception
    {
    	super.save(fileName);
		this.controller.saved(getName());
    }
    
	@Override
	public void close() throws Exception
	{
		super.close();
		this.controller.close();
	}

	//============================================================
	// private methods
	//============================================================
	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(CsvFxController.class.getResource("CsvFx.fxml"));
			this.controller.init(this, getFactory().getSettings());
			Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}

	private boolean isControllerInit = false;
	private DataProvider<String> provider;
	private CsvFxController controller;
}
