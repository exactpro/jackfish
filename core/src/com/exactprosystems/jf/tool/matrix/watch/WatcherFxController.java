////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.watch;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.fields.NewExpressionField;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Window;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class WatcherFxController implements Initializable, ContainingParent
{
	public NewExpressionField expressionField;
	public Button btnAddNewExpression;
	public Button btnAddAll;
	public Button btnClose;
	public GridPane mainGrid;

	private CustomTable<Settings.SettingsValue> table;
	private Dialog<ButtonType> dialog;
	private WatcherFx model;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert btnAddNewExpression != null : "fx:id=\"btnAddNewExpression\" was not injected: check your FXML file 'WatcherFx.fxml'.";
		assert btnClose != null : "fx:id=\"btnClose\" was not injected: check your FXML file 'WatcherFx.fxml'.";
		assert btnAddAll != null : "fx:id=\"btnAddAll\" was not injected: check your FXML file 'WatcherFx.fxml'.";
		this.table = new CustomTable<>(true);
		this.table.setOnDragDropped(event -> {
			Dragboard dragboard = event.getDragboard();
			boolean b = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				this.model.newVariable(str, true);
				b = true;
			}
			event.setDropCompleted(b);
			event.consume();
		});
		this.table.setOnDragOver(event ->
		{
			if (event.getGestureSource() != this.table && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});
		mainGrid.add(this.table, 0, 0);
		GridPane.setColumnSpan(this.table, 3);
	}

	@Override
	public void setParent(Parent parent)
	{
		dialog = new Alert(Alert.AlertType.INFORMATION);
		dialog.setTitle("Watcher");
		dialog.getDialogPane().setContent(parent);
		dialog.initModality(Modality.NONE);
		dialog.setResizable(true);
	}

	public void init(Window owner, WatcherFx model, AbstractEvaluator evaluator, Matrix matrix)
	{
		this.dialog.initOwner(owner);
		this.model = model;
		this.table.setListener(this.model::removeItems);
		this.expressionField = new NewExpressionField(evaluator);
		this.expressionField.setHelperForExpressionField("Watcher", matrix);
		this.mainGrid.add(expressionField, 0, 1);
		GridPane.setMargin(this.expressionField, new Insets(0, 5, 0, 5));
		this.table.completeFirstColumn("Expression", "key", true, false);
		this.table.completeSecondColumn("Result", "value", false, true);
		this.table.onFinishEditFirstColumn((value, newValue) -> this.model.updateRow(newValue, this.table.getItems().indexOf(value)));
		listeners();
	}

	public void show(String text)
	{
		if (!isShow())
		{
			this.dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
			this.dialog.setHeaderText("Watcher for " + text);
			Optional<ButtonType> buttonType = this.dialog.showAndWait();
			buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(bt -> Common.tryCatch(this.model::saveData, "Error on close watcher"));
		}
	}

	public void displayData(List<Settings.SettingsValue> values)
	{
		this.table.getItems().clear();
		this.table.setItems(FXCollections.observableArrayList(values));
		this.table.update();
	}

	public void displayNewVariable(Settings.SettingsValue value)
	{
		this.table.getItems().add(value);
		this.expressionField.clear();
	}

	public void updateRow(Settings.SettingsValue settingsValue, int rowIndex)
	{
		this.table.getItems().set(rowIndex, settingsValue);
	}

	public boolean isShow()
	{
		return this.dialog != null && this.dialog.isShowing();
	}

	public void close()
	{
		this.dialog.close();
	}

	//============================================================
	// events methods
	//============================================================
	public void addAll(ActionEvent event)
	{
		Common.tryCatch(this.model::addAllVariables, "Error on add all variables");
	}

	public void addNewExpression(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.newVariable(this.expressionField.getText(), true), "Error on add new expression");
	}

	//============================================================
	// private methods
	//============================================================
	private void listeners()
	{
		this.expressionField.setOnAction(this::addNewExpression);
		this.dialog.setOnShowing(windowEvent -> model.afterRendering());
	}
}
