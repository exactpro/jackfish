////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.custom.console.CustomListView;

import javafx.application.Platform;

import java.io.OutputStream;
import java.io.PrintStream;

public class TabConsole extends PrintStream
{
	public CustomListView<MatrixItem> listView; // TODO

	public TabConsole(OutputStream out)
	{
		super(out);
	}
	
	public TabConsole makeCopy()
	{
	    TabConsole res = new TabConsole(this.out);
	    res.listView = this.listView;
	    return res;
	}

	@Override
	public void print(final String s)
	{
	    if (this.listView == null)
	    {
	        super.print(s);
	    }
	    else
	    {
			listView.getItems().add(ConsoleText.defaultText(s));
	    }
	}

	@Override
	public void println(final String x)
	{
        if (this.listView == null)
        {
            super.print(x);
        }
        else
        {
			listView.getItems().add(ConsoleText.defaultText(x));
		}
	}

	public void setConsole(CustomListView<MatrixItem> listView)
	{
		this.listView = listView;
	}
}