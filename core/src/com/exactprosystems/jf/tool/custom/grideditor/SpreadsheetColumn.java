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
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.stream.Collectors;

public final class SpreadsheetColumn {

	public static boolean isColumnEditable = false;

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

    private ContextMenu getColumnContextMenu()
	{
		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);

		MenuItem addColumnBefore = new MenuItem("Add column before");
		addColumnBefore.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem addColumnAfter = new MenuItem("Add column after");
		addColumnAfter.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this) + 1));

		MenuItem moveLeftColumn = new MenuItem("Move left this column");
		moveLeftColumn.setOnAction(e ->
			{
				int currentColumn = this.spreadsheetView.getColumns().indexOf(this);
				this.spreadsheetView.swapColumns(currentColumn, currentColumn -1);
			}
		);

		MenuItem moveRightColumn = new MenuItem("Move right this column");
		moveRightColumn.setOnAction(e ->
			{
				int currentColumn = this.spreadsheetView.getColumns().indexOf(this);
				this.spreadsheetView.swapColumns(currentColumn, currentColumn +1);
			}
		);

		MenuItem removeColumn = new MenuItem("Remove columns");
		removeColumn.setOnAction(e -> this.spreadsheetView.removeColumns(
				this.spreadsheetView.getSelectionModel()
						.getSelectedCells()
						.stream()
						.map(TablePosition::getColumn)
						.distinct()
						.collect(Collectors.toList())
				)
		);

		MenuItem renameColumn = new MenuItem("Rename");
		renameColumn.setOnAction(e -> startRenameColumn());

		MenuItem switchOffColumn = new MenuItem("Switch off");
		switchOffColumn.setOnAction(e -> this.spreadsheetView.switchColumn(false, this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem switchOnColumn = new MenuItem("Switch on");
		switchOnColumn.setOnAction(e -> this.spreadsheetView.switchColumn(true, this.spreadsheetView.getColumns().indexOf(this)));

		contextMenu.getItems().addAll(addColumnBefore,addColumnAfter, removeColumn, renameColumn, switchOffColumn, switchOnColumn, moveLeftColumn, moveRightColumn);
		return contextMenu;
    }

	public void startRenameColumn()
	{
		isColumnEditable = true;
		String oldValue = this.column.getText();
		TextField tf = new TextField(oldValue);
		tf.setMaxWidth(this.column.getWidth());
		tf.setPrefWidth(this.column.getWidth());
		this.column.setGraphic(tf);
		tf.toFront();
		tf.requestFocus();
		tf.setOnKeyPressed(e1 -> {
			if (e1.getCode().equals(KeyCode.ENTER))
			{
				this.spreadsheetView.renameColumn(this, tf.getText());
				this.column.setGraphic(null);
				isColumnEditable = false;
			}
			if (e1.getCode().equals(KeyCode.ESCAPE))
			{
				this.column.setGraphic(null);
				isColumnEditable = false;
			}
		});

		tf.focusedProperty().addListener((observable, oldValue1, newValue) -> {
			if (!newValue && !tf.getText().equals(column.getText()))
			{
				this.spreadsheetView.renameColumn(this, tf.getText());
				this.column.setGraphic(null);
				isColumnEditable = false;
			}
			else
			{
				this.column.setGraphic(null);
				isColumnEditable = false;
			}
		});
	}
}
