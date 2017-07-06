////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.xmltree;

import com.exactprosystems.jf.tool.wizard.related.XmlItem;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;

import javafx.scene.control.TreeTableColumn;
import javafx.scene.control.TreeTableView;

class MyCustomSkin extends TreeTableViewSkin<XmlItem>
{
    public MyCustomSkin(TreeTableView<XmlItem> treeTableView)
	{
		super(treeTableView);
	}

	public void show(int index)
	{
		flow.show(index);
	}

	public boolean isIndexVisible(int index)
	{
		return flow.getFirstVisibleCell() != null &&
				flow.getLastVisibleCell() != null &&
				flow.getFirstVisibleCell().getIndex() <= index - 1 &&
				flow.getLastVisibleCell().getIndex() >= index + 1;
	}

	@Override
	public void resizeColumnToFitContent(TreeTableColumn<XmlItem, ?> tc, int maxRows)
	{
		super.resizeColumnToFitContent(tc, maxRows);
	}
}