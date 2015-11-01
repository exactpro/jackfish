////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;

import javafx.application.Platform;

import java.io.OutputStream;
import java.io.PrintStream;

public class TabConsole extends PrintStream
{
	private CustomListView<MatrixItem> listView;

	public TabConsole(OutputStream out)
	{
		super(out);
	}

	@Override
	public void print(final String s)
	{
		Platform.runLater(() -> listView.getItems().add(ConsoleText.defaultText(s)));
	}

	@Override
	public void println(final String x)
	{
		Platform.runLater(() -> listView.getItems().add(ConsoleText.defaultText(x)));
	}

	public void setConsole(CustomListView<MatrixItem> listView)
	{
		this.listView = listView;
	}
}