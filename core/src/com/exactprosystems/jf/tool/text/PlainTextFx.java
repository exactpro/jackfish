////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.text;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.DocumentFactory;
import com.exactprosystems.jf.documents.text.PlainText;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.scene.control.ButtonType;

import java.util.Optional;

public class PlainTextFx extends PlainText
{
	public PlainTextFx(String fileName, DocumentFactory factory)
	{
		super(fileName, factory);
	}

	//region AbstractDocument
	@Override
	public void display() throws Exception
	{
		super.display();

		initController();

		this.controller.displayTitle(Common.getSimpleTitle(getName()));
		this.controller.displayText(super.property);
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
	public void close(Settings settings) throws Exception
	{
		super.close(settings);
		this.controller.close();
	}
	//endregion

	private void initController()
	{
		if (!this.isControllerInit)
		{
			this.controller = Common.loadController(PlainTextFxController.class.getResource("PlainTextFx.fxml"));
			this.controller.init(this, getFactory().getSettings());
			Optional.ofNullable(getFactory().getConfiguration()).ifPresent(c -> c.register(this));
			this.isControllerInit = true;
		}
	}

	private boolean isControllerInit = false;
	private PlainTextFxController controller;
}
