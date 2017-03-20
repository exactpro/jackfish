package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.CssVariables;
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
			switch (state)
			{
				case BreakPoint:	setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON))); return;
				case Executing:
					if (((MatrixTreeView) getTreeTableRow().getTreeTableView()).isTracing())
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
					return;
				case ExecutingWithBreakPoint:
					if (((MatrixTreeView) getTreeTableRow().getTreeTableView()).isTracing())
					{
						setGraphic(new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON)));
					}
					return;
			}
		}
	}
}