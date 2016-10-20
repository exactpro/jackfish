package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconCell extends TreeTableCell<MatrixItem, MatrixItemState>
{
	private static final ImageView BREAK_POINT_IMAGE_VIEW = new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON));
	private static final ImageView EXECUTING_IMAGE_VIEW = new ImageView(new Image(CssVariables.Icons.EXECUTING_ITEM_ICON));

	public IconCell()
	{
	}

	@Override
	public void updateItem(MatrixItemState state, boolean empty)
	{
		super.updateItem(state, empty);
		if (state != null)
		{
			switch (state)
			{
				case BreakPoint:	setGraphic(BREAK_POINT_IMAGE_VIEW); return;
				case Executing:		setGraphic(EXECUTING_IMAGE_VIEW); return;
			}
		}
		else
		{
			setGraphic(null);
		}
	}
}