////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.DocumentInfo;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.tool.AbstractDocument;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ButtonType;

import java.io.*;
import java.util.Optional;

@DocumentInfo(
		newName = "NewText", 
		extentioin = "txt", 
		description = "Plain text"
)
public class PlainTextFx extends AbstractDocument
{
	public PlainTextFx(String fileName, Settings settings, Configuration config)
	{
		super(fileName, config);
		this.settings = settings;
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

	//==============================================================================================================================
	// AbstractDocument
	//==============================================================================================================================
	@Override
	public void display() throws Exception
	{
		super.display();
		
		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayText(property);
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
		this.property.set(read(reader));
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
    	write(fileName);
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
        return this.changed;
	}

	@Override
	public void saved()
	{
		super.saved();
        this.changed = false;
    }
	
    //------------------------------------------------------------------------------------------------------------------
	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(PlainTextFxController.class.getResource("PlainTextFx.fxml"));
			this.controller.init(this, this.settings);
			Optional.ofNullable(getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}

	private String read(Reader reader) throws IOException
	{
		StringBuilder sb = new StringBuilder();
		try (BufferedReader buffReader = new BufferedReader(reader))
		{
			String line = null;
			while((line = buffReader.readLine()) != null)
			{
				sb.append(line).append('\n');
			}
		}
		return sb.toString();
	}

	private void write(String fileName) throws IOException
	{
		try (Writer writer = new FileWriter(fileName);
			BufferedWriter buffWriter = new BufferedWriter(writer))
		{
			for (String line : this.property.get().split("\n"))
			{
				buffWriter.write(line);
				buffWriter.newLine();
			}
		}
	}

	private boolean isControllerInit = false;
	private StringProperty property;
	private Settings settings;
	private boolean changed = false;
	private PlainTextFxController controller;
}
