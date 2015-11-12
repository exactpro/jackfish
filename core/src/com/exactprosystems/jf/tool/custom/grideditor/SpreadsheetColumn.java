////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.Str;
import javafx.beans.Observable;
import javafx.collections.ObservableList;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;

import java.util.List;

public final class SpreadsheetColumn {

    private final SpreadsheetView spreadsheetView;
    final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;
    private final Integer indexColumn;

	SpreadsheetColumn(final TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column,final SpreadsheetView spreadsheetView, final Integer indexColumn, DataProvider provider) {
		this.spreadsheetView = spreadsheetView;
		this.column = column;
		column.setMinWidth(50);
		this.indexColumn = indexColumn;

		column.setContextMenu(getColumnContextMenu());

		provider.getColumnHeaders().addListener((Observable arg0) -> {
			List<String> columnsHeader = spreadsheetView.getProvider().getColumnHeaders();
			setText(Str.asString(columnsHeader.get(indexColumn)));
		});
	}

    public void setPrefWidth(double width) {
        column.setPrefWidth(Math.ceil(width));
        spreadsheetView.columnWidthSet(indexColumn);
    }

    public double getWidth() {
        return column.getWidth();
    }

    public void setText(String text) {
        column.setText(text);
    }

    public String getText() {
        return column.getText();
    }

    private ContextMenu getColumnContextMenu() {
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);
		MenuItem addColumnBefore = new MenuItem("Add column before");
		addColumnBefore.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem addColumnAfter = new MenuItem("Add column after");
		addColumnAfter.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this) + 1));

		MenuItem removeColumn = new MenuItem("Remove");
		removeColumn.setOnAction(e -> this.spreadsheetView.removeColumn(this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem renameColumn = new MenuItem("Rename");
		renameColumn.setOnAction(e -> {
			String oldValue = this.column.getText();
			TextField tf = new TextField(oldValue);
			this.column.setGraphic(tf);
			tf.requestFocus();
			tf.setOnKeyPressed(e1 -> {
				if (e1.getCode().equals(KeyCode.ESCAPE))
				{
					this.spreadsheetView.renameColumn(this, oldValue);
					this.column.setGraphic(null);
				}
				if (e1.getCode().equals(KeyCode.TAB) || e1.getCode().equals(KeyCode.ENTER))
				{
					this.spreadsheetView.renameColumn(this, tf.getText());
					this.column.setGraphic(null);
				}
			});
			tf.focusedProperty().addListener((observable, oldValue1, newValue) -> {
				if (!newValue && oldValue1)
				{
					if (this.column.getGraphic() != null)
					{
						this.spreadsheetView.renameColumn(this, oldValue);
						this.column.setGraphic(null);
					}
				}
			});
		});
		contextMenu.getItems().addAll(addColumnBefore,addColumnAfter, removeColumn, renameColumn);
		return contextMenu;
    }
}
