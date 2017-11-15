////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SpreadsheetGridView extends TableView<ObservableList<SpreadsheetCell>>
{
	private final SpreadsheetHandle handle;
	private boolean editWithEnter = false;
	private boolean editWithKey = false;



	public SpreadsheetGridView(SpreadsheetHandle handle)
	{
		this.handle = handle;
	}

	@Override
	protected javafx.scene.control.Skin<?> createDefaultSkin()
	{
		return new GridViewSkin(handle);
	}
	public boolean getEditWithEnter(){
		return editWithEnter;
	}

	public void setEditWithEnter(boolean b){
		editWithEnter = b;
	}

	public void setEditWithKey(boolean b){
		editWithKey = b;
	}

	public boolean getEditWithKey(){
		return editWithKey;
	}
}
