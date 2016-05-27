package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

import javafx.geometry.Insets;
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
			int treeItemLevel = getTreeTableView().getTreeItemLevel(getTreeTableRow().getTreeItem());
			int left = 10 * treeItemLevel;
			gridView.setPadding(new Insets(0, 0, 0, left));
			setGraphic(gridView);
		}
		else
		{
			setGraphic(null);
		}
	}
}
