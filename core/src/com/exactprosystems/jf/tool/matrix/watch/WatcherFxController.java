////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.watch;

import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.table.CustomTable;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.VPos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

import static com.exactprosystems.jf.tool.custom.table.CustomTable.EditState;

public class WatcherFxController implements Initializable, ContainingParent
{
	public ExpressionField expressionField;
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
		this.table = new CustomTable<>(false);
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
		DialogsHelper.centreDialog(this.dialog);
		Common.addIcons(((Stage) this.dialog.getDialogPane().getScene().getWindow()));
		dialog.getDialogPane().setContent(parent);
		dialog.initModality(Modality.NONE);
		dialog.setResizable(true);
	}

	public void init(Window owner, WatcherFx model, AbstractEvaluator evaluator, Matrix matrix)
	{
		this.dialog.initOwner(owner);
		this.model = model;
		this.table.setDeleteListener(this.model::removeItems);
		this.expressionField = new ExpressionField(evaluator);
		this.expressionField.setHelperForExpressionField(R.WATCHER_FX_CONTR_WATCHER.get(), matrix);
		this.expressionField.setMaxWidth(1.7976931348623157E308);
		this.mainGrid.add(expressionField, 0, 1);
		this.mainGrid.setPadding(new Insets(0,16,5,16));
		this.dialog.getDialogPane().setHeader(new Label());
		GridPane.setValignment(this.expressionField, VPos.BOTTOM);
		this.table.completeFirstColumn(R.COMMON_EXPRESSION.get(), "key", EditState.TEXTFIELD, false);
		this.table.completeSecondColumn(R.COMMON_RESULT.get(), "value", EditState.TEXTFIELD_READONLY, true);
		this.table.onFinishEditFirstColumn((value, newValue) -> this.model.updateRow(newValue, this.table.getItems().indexOf(value)));
		listeners();
	}

	public void show(String text)
	{
		if (!isShow())
		{
			this.dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
			this.dialog.setTitle(String.format(R.WATCHER_FX_CONTR_WATCHER_FOR.get(), text));
			Optional<ButtonType> buttonType = this.dialog.showAndWait();
			buttonType.filter(bt -> bt.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(bt -> Common.tryCatch(this.model::saveData, R.WATCHER_FX_CONTR_ERROR_CLOSE.get()));
		}
	}

	public void displayData(List<Settings.SettingsValue> values)
	{
		Common.runLater(() -> {
			this.table.getItems().clear();
			this.table.setItems(FXCollections.observableArrayList(values));
			this.table.update();
		});
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
		Common.tryCatch(this.model::addAllVariables, R.WATCHER_FX_CONTR_ERROR_ALL.get());
	}

	public void addNewExpression(ActionEvent event)
	{
		Common.tryCatch(() -> this.model.newVariable(this.expressionField.getText(), true), R.WATCHER_FX_CONTR_ERROR_NEW.get());
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
