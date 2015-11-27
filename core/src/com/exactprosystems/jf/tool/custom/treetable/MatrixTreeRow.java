package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.TreeTableRow;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MatrixTreeRow extends TreeTableRow<MatrixItem>
{
	public MatrixTreeRow(ContextMenu contextMenu) throws Exception
	{
		setContextMenu(contextMenu);
	}

	@Override
	protected void updateItem(MatrixItem item, boolean empty)
	{
		super.updateItem(item, empty);
		this.getStyleClass().removeAll(CssVariables.ITEM_OFF_TRUE, CssVariables.ITEM_OFF_FALSE);
		if (item != null)
		{
			String style = CssVariables.ITEM_OFF_FALSE;
			if (!item.canExecute())
			{
				style = CssVariables.ITEM_OFF_TRUE;
			}
			this.getStyleClass().add(style);
		}
	}

	//TODO do this method's
	public void showExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.forEach(ExpressionField::showShadowText);
	}

	public void hideExpressionsResults()
	{
		List<ExpressionField> list = new ArrayList<>();
		find((GridPane) this.getTreeTableView().getSelectionModel().getSelectedItem().getValue().getLayout(), list);
		list.forEach(ExpressionField::hideShadowText);
	}

	private void find(Pane parent, List<ExpressionField> fields)
	{
		//TODO not work
		Optional.of(parent).filter(p -> p instanceof ExpressionField).ifPresent(p -> fields.add(((ExpressionField) p)));
		fields.addAll(parent.getChildren().stream().filter(n -> n instanceof ExpressionField).map(n -> ((ExpressionField) n)).collect(Collectors.toList()));
		parent.getChildren().stream().filter(n -> n instanceof Pane && !(n instanceof ExpressionField)).forEach(pane -> find(((Pane) pane), fields));
	}

}