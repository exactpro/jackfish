package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.CssVariables;

import javafx.scene.control.Label;
import javafx.scene.control.TreeTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

public class IconCell extends TreeTableCell<MatrixItem, MatrixItemState>
{
	private Label label = new Label();

	public IconCell()
	{
	}

	@Override
	public void updateItem(MatrixItemState state, boolean empty)
	{
		super.updateItem(state, empty);
		if (state != null)
		{
			Image image = null;
			switch (state)
			{
				case BreakPoint:
					image = new Image(CssVariables.Icons.BREAK_POINT_ICON);
					break;
				case Executing:
					image = new Image(CssVariables.Icons.EXECUTING_ITEM_ICON);
					break;
				default:
					break;
			}
			label.setGraphic(new ImageView(image));
			label.setMinWidth(20);
			setGraphic(this.label);
		}
		else
		{
			setGraphic(null);
		}
	}
}