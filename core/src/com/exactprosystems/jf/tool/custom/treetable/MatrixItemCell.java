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
