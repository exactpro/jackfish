////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.console;

import com.exactprosystems.jf.tool.Common;
import javafx.scene.control.ListCell;

@Deprecated
public class ColorCell<T> extends ListCell<ConsoleText<T>>
{
	@Override
	protected void updateItem(final ConsoleText<T> s, boolean b)
	{
		super.updateItem(s, b);
		Common.runLater(() -> setGraphic(s));
	}
}
