////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.Variables;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.helper.SimpleVariable;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Dialog;
import javafx.scene.control.DialogPane;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
//import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;
import java.util.stream.Collectors;

public class UserEditTableDialog extends Dialog<Boolean>
{
	private final GridPane grid;
	private final Label expressionLabel;
	private final TableView<RowTable> tableView;

	public UserEditTableDialog(String title, Table table, Map<String, Boolean> columns)
	{
		final DialogPane dialogPane = getDialogPane();
		this.setResizable(true);
		dialogPane.getStylesheets().addAll(Common.currentThemesPaths());
		this.expressionLabel = new Label();
		
		this.tableView = createTableView(table, columns);

		GridPane.setHgrow(this.tableView, Priority.ALWAYS);
		GridPane.setFillWidth(this.tableView, true);

		GridPane.setHgrow(this.expressionLabel, Priority.ALWAYS);
		GridPane.setFillWidth(this.expressionLabel, true);

		this.grid = new GridPane();
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.contentTextProperty().addListener(o -> updateGrid());
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		dialogPane.setPrefWidth(550);
		dialogPane.setMaxWidth(550);
		dialogPane.setMinWidth(550);
		dialogPane.setPrefHeight(200);
		dialogPane.setMinHeight(200);
		dialogPane.setMaxHeight(200);

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
            String name = entry.getKey();
            Boolean editable = entry.getValue();

            if (table.columnIsPresent(name))
            {
                final TableColumn<RowTable, ?> column = new TableColumn<>();
                column.setText(name);
                column.setCellValueFactory(new PropertyValueFactory<>(name));
                column.setEditable(editable);
                tableView.getColumns().add(column);
            }
        }

//        tableView.setRowFactory(tableView1 -> new ColorRow(clazz));

        
        
        ObservableList<RowTable> data = FXCollections.observableArrayList();

        for (RowTable a : table)
        {
            data.add(a);
        }
        
        tableView.setItems(data);

        return tableView;
    }

//    public void fillVariables(final ObservableList<SimpleVariable> data)
//    {
//        Variables localVars = this.evaluator.getLocals();
//        localVars.getVars().entrySet()
//                .forEach((entry) -> data.add(new SimpleVariable(entry.getKey(), entry.getValue())));
//
//        Variables globalVars = this.evaluator.getGlobals();
//        data.addAll(globalVars.getVars().entrySet().stream()
//                .map(entry -> new SimpleVariable(entry.getKey(), entry.getValue()))
//                .filter(simpleVariable -> !data.contains(simpleVariable)).collect(Collectors.toList()));
//    }
//	
    private void updateGrid()
	{
		grid.getChildren().clear();

		grid.add(this.tableView, 0, 0);
		grid.add(this.expressionLabel, 0, 1);
		getDialogPane().setContent(grid);

		Platform.runLater(tableView::requestFocus);
	}
    
    private class ColorRow extends TableRow<RowTable>
    {
//        private Class<?>    expectedClazz;
//
//        public ColorRow(Class<?> expectedClazz)
//        {
//            this.expectedClazz = expectedClazz;
//        }

        @Override
        protected void updateItem(RowTable e, boolean b)
        {
            super.updateItem(e, b);
            if (e == null)
            {
                return;
            }
//            if (expectedClazz != null && e.getClazz() != null && e.getClazz().equals(expectedClazz.getSimpleName()))
//            {
//                this.getStyleClass().addAll(CssVariables.EXPECTED_CLASS);
//            }
        }
    }

}
