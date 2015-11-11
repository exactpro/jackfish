////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;
import javafx.scene.control.TableView;

public class SpreadsheetGridView extends TableView<ObservableList<SpreadsheetCell>>
{
	private final SpreadsheetHandle handle;

	public SpreadsheetGridView(SpreadsheetHandle handle)
	{
		this.handle = handle;
	}

	@Override
	protected javafx.scene.control.Skin<?> createDefaultSkin()
	{
		return new GridViewSkin(handle);
	}
};
