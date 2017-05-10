package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconCell extends TreeTableCell<MatrixItem, MatrixItemState>
{
	public IconCell()
	{
	}

	@Override
	public void updateItem(MatrixItemState state, boolean empty)
	{
		super.updateItem(state, empty);
		setGraphic(null);
		if (state != null)
		{
			boolean isTracing = ((MatrixTreeView) getTreeTableRow().getTreeTableView()).isTracing();
			TreeItem<MatrixItem> treeItem = getTreeTableRow().getTreeItem();
			switch (state)
			{
				case BreakPoint:	setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON))); return;
				case Executing:
					if (isTracing)
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
					return;

				case ExecutingWithBreakPoint:
					setGraphic(new ImageView(new Image(isTracing ? CssVariables.Icons.EXECUTING_BREAK_POINT_ICON : CssVariables.Icons.BREAK_POINT_ICON)));
					return;

				case ExecutingParent:
					if (isTracing && treeItem != null && !treeItem.isExpanded())
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
			}
		}
	}
}