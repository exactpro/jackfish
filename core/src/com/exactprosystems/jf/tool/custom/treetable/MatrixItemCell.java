/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.layout.GridPane;

public class MatrixItemCell extends TreeTableCell<MatrixItem, MatrixItem>
{
	@Override
	protected void updateItem(MatrixItem item, boolean empty)
	{
		super.updateItem(item, empty);
		if (item != null && item.getLayout() instanceof GridPane)
		{
			GridPane gridView = (GridPane)item.getLayout();
			setGraphic(gridView);
		}
		else
		{
			setGraphic(null);
		}
	}
}
