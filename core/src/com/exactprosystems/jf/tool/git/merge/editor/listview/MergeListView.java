////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.git.merge.editor.listview;

import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

public class MergeListView extends ListView<MergeCell>
{
	public MergeListView()
	{
		this.setCellFactory(p -> new ListCell<MergeCell>(){
			@Override
			protected void updateItem(MergeCell item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !empty)
				{
					MergeCell.MergeType mergeType = item.getMergeType();
					setGraphic(new Text(item.getText()));
				}
				else
				{
					setGraphic(null);
				}
			}
		});
	}
}
