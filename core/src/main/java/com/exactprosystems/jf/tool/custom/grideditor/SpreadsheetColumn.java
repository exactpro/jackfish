/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
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

		MenuItem addColumnBefore = new MenuItem(R.SSC_ADD_BEFORE.get());
		addColumnBefore.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem addColumnAfter = new MenuItem(R.SSC_ADD_AFTER.get());
		addColumnAfter.setOnAction(e -> this.spreadsheetView.addColumn(this.spreadsheetView.getColumns().indexOf(this) + 1));

		MenuItem moveLeftColumn = new MenuItem(R.SSC_MOVE_LEFT.get());
		moveLeftColumn.setOnAction(e ->
			{
				int currentColumn = this.spreadsheetView.getColumns().indexOf(this);
				this.spreadsheetView.swapColumns(currentColumn, currentColumn -1);
			}
		);

		MenuItem moveRightColumn = new MenuItem(R.SSC_MOVE_RIGTH.get());
		moveRightColumn.setOnAction(e ->
			{
				int currentColumn = this.spreadsheetView.getColumns().indexOf(this);
				this.spreadsheetView.swapColumns(currentColumn, currentColumn +1);
			}
		);

		MenuItem removeColumn = new MenuItem(R.SSC_REMOVE.get());
		removeColumn.setOnAction(e -> this.spreadsheetView.removeColumns(
				this.spreadsheetView.getSelectionModel()
						.getSelectedCells()
						.stream()
						.map(TablePosition::getColumn)
						.distinct()
						.collect(Collectors.toList())
				)
		);

		MenuItem renameColumn = new MenuItem(R.SSC_RENAME.get());
		renameColumn.setOnAction(e -> startRenameColumn());

		MenuItem switchOffColumn = new MenuItem(R.SSC_SWITCH_OFF.get());
		switchOffColumn.setOnAction(e -> this.spreadsheetView.switchColumn(false, this.spreadsheetView.getColumns().indexOf(this)));

		MenuItem switchOnColumn = new MenuItem(R.SSC_SWITCH_ON.get());
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
			if (!this.isColumnEditable)
			{
				return;
			}
			if (e1.getCode().equals(KeyCode.ENTER))
			{
				this.isColumnEditable = false;
				this.spreadsheetView.renameColumn(this, tf.getText());
				this.column.setGraphic(null);
			}
			if (e1.getCode().equals(KeyCode.ESCAPE))
			{
				isColumnEditable = false;
				this.column.setGraphic(null);
			}
		});

		tf.focusedProperty().addListener((observable, oldValue1, newValue) -> {
			if (!this.isColumnEditable)
			{
				return;
			}
			if (!newValue && !tf.getText().equals(column.getText()))
			{
				isColumnEditable = false;
				this.spreadsheetView.renameColumn(this, tf.getText());
				this.column.setGraphic(null);
			}
			else
			{
				isColumnEditable = false;
				this.column.setGraphic(null);
			}
		});
	}
}
