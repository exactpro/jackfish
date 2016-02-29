////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.csv;

import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.DocumentInfo;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.AbstractDocument;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.io.FileReader;
import java.io.Reader;
import java.util.Optional;

@DocumentInfo(
		newName 	= "NewCsv", 
		extentioin 	= "csv", 
		description = "CSV"
)
public class CsvFx extends AbstractDocument
{
	private char tableDelimiter = ';';

	public CsvFx(String fileName, Settings settings, Configuration config)
	{
		super(fileName, config);
		
		this.settings = settings;
		this.table = new Table(new String[][]
				{
						new String[] {	"<none>" },
						new String[] { 	"" },
				}, null);
		this.provider = new TableDataProvider(this.table);
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		
		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayTable(this.provider);
	}

	@Override
	public void create() throws Exception
	{
		super.create();
		initController();
	}
	
	@Override
	public void load(Reader reader) throws Exception
	{
		super.load(reader);

		this.table = new Table(reader, tableDelimiter, null);
		this.provider = new TableDataProvider(this.table);
		//TODO implements undo redo
		initController();
	}

    @Override
    public boolean canClose()  throws Exception
    {
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
    	this.table.save(fileName, tableDelimiter, false, false);
		this.controller.saved(getName());
    }
    
	@Override
	public void close(Settings settings) throws Exception
	{
		super.close(settings);
		this.controller.close();
	}

    //------------------------------------------------------------------------------------------------------------------
    // interface Mutable
    //------------------------------------------------------------------------------------------------------------------
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

	public void reloadCsv() throws Exception
	{
		if (hasName())
		{
			this.load(new FileReader(getName()));
			this.display();
		}
	}

	//============================================================
	// private methods
	//============================================================
	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(CsvFxController.class.getResource("CsvFx.fxml"));
			this.controller.init(this, this.settings);
			Optional.ofNullable(getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}

	private boolean isControllerInit = false;
	private Table table;
	private DataProvider<String> provider;
	private Settings settings;
	private CsvFxController controller;
}
