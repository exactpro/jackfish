////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.csv;

import java.io.File;
import java.io.Reader;

import javafx.scene.control.ButtonType;

import com.exactprosystems.jf.common.DocumentInfo;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.AbstractDocument;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

@DocumentInfo(
		newName 	= "NewCsv", 
		extentioin 	= "csv", 
		description = "CSV"
)
public class CsvFx extends AbstractDocument
{
	public CsvFx(String fileName, Settings settings)
	{
		super(fileName);
		this.settings = settings;
		this.table = new Table(new String[][]
				{
						new String[] {	"<none>" },
						new String[] { 	"" },
				});
	}

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		
		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayTable(this.table);
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
		
		this.table = new Table(reader, ','); // TODO
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
		if (!hasName())
		{
			File file = DialogsHelper.showSaveAsDialog(this);
			if (file == null)
			{
				return;
			}
			
			fileName = file.getPath();
		}
		
    	super.save(fileName);
    	this.table.save(fileName, ','); // TODO
		this.controller.saved(getName());
    }
    
	@Override
	public void close() throws Exception
	{
		super.close();
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
	
    //------------------------------------------------------------------------------------------------------------------
	private void initController()
	{
		this.controller = Common.loadController(CsvFxController.class.getResource("CsvFx.fxml"));
		this.controller.init(this, this.settings);
	}

	private Table table;
	private Settings settings;
	private CsvFxController controller;
}
