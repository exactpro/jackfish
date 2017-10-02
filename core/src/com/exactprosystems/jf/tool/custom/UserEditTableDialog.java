////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Map;
import java.util.Map.Entry;

public class UserEditTableDialog extends Dialog<Boolean>
{
	private final GridPane grid;
	private final TableView<RowTable> tableView;

	public UserEditTableDialog(Table table, Map<String, Boolean> columns)
	{
		final DialogPane dialogPane = getDialogPane();
		this.setResizable(true);
		dialogPane.getStylesheets().addAll(Theme.currentThemesPaths());

		this.tableView = createTableView(table, columns);

		GridPane.setHgrow(this.tableView, Priority.ALWAYS);
		GridPane.setFillWidth(this.tableView, true);

		this.grid = new GridPane();
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setMaxHeight(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.contentTextProperty().addListener(o -> updateGrid());
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		dialogPane.setPrefWidth(550);

		updateGrid();

		setResultConverter((dialogButton) ->
		{
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonBar.ButtonData.OK_DONE;
		});
	}

	private TableView<RowTable> createTableView(Table table, Map<String, Boolean> columns)
    {
        TableView<RowTable> tableView = new TableView<>();
        tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        
        for (Entry<String, Boolean> entry : columns.entrySet())
        {
            final String name = entry.getKey();
            final Boolean editable = entry.getValue();

            if (table.columnIsPresent(name))
            {
                final TableColumn<RowTable, String> column = new TableColumn<>();
                column.setText(name);
                column.setCellValueFactory(p -> new SimpleObjectProperty<>("" + p.getValue().get(name)));

                if (editable)
                {
                    column.setEditable(true);
                    column.setCellFactory(p -> new TableCell<RowTable, String>()
                    {
                        @Override
                        protected void updateItem(String item, boolean empty)
                        {
                            super.updateItem(item, empty);
                            if (item != null && !empty)
                            {
                                TextField value = new TextField(item.toString());
                                value.textProperty().addListener((observable, oldValue, newValue) -> 
                                {
                                    ((RowTable) getTableRow().getItem()).put(name,newValue);
                                });
                                value.getStyleClass().setAll(CssVariables.EDITABLE_PARAMETER);
                                setGraphic(value);
                            }
                            else
                            {
                                setText(null);
                            }
                        }
                    });
                }
                tableView.getColumns().add(column);
            }
        }
        
        ObservableList<RowTable> data = FXCollections.observableArrayList(table);
        tableView.setItems(data);

        return tableView;
    }

    private void updateGrid()
	{
		grid.getChildren().clear();

		grid.add(this.tableView, 0, 0);
		getDialogPane().setContent(grid);

		Common.runLater(tableView::requestFocus);
	}
    
}
