/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.documents.guidic.actions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.beans.property.SimpleObjectProperty;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.stage.Modality;

import static javafx.scene.control.TableView.CONSTRAINED_RESIZE_POLICY;

public class EditVariableDialog extends Dialog<Parameters>
{

	EditVariableDialog(Parameters parameters, AbstractEvaluator evaluator)
	{
		super();
		super.setTitle(R.EDIT_VARIABLE_DIALOG_TITLE.get());
		super.initModality(Modality.APPLICATION_MODAL);
		super.initOwner(Common.node);
		super.setResizable(true);
		parameters.evaluateAll(evaluator);

		DialogPane dialogPane = super.getDialogPane();
		dialogPane.getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
		dialogPane.setPrefSize(800.0, 800.0);

		TableView<Parameter> tableView = new TableView<>();
		tableView.setEditable(true);
		tableView.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);

		TableColumn<Parameter, String> nameColumn = new TableColumn<>(R.EDIT_VARIABLE_DIALOG_NAME.get());
		nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
		nameColumn.setCellFactory(tsTableColumn -> new CustomTableCell());
		nameColumn.setEditable(true);
		nameColumn.setOnEditCommit(event -> event.getRowValue().setName(event.getNewValue()));

		TableColumn<Parameter, String> expressionColumn = new TableColumn<>(R.EDIT_VARIABLE_DIALOG_EXPRESSION.get());
		expressionColumn.setCellValueFactory(new PropertyValueFactory<>("expression"));
		expressionColumn.setCellFactory(tsTableColumn -> new CustomTableCell());
		expressionColumn.setEditable(true);
		expressionColumn.setOnEditCommit(event ->
		{
			event.getRowValue().setExpression(event.getNewValue());
			event.getRowValue().evaluate(evaluator);
			this.update(tableView);
		});

		TableColumn<Parameter, String> valueColumn = new TableColumn<>(R.EDIT_VARIABLE_DIALOG_VALUE.get());
		valueColumn.setCellValueFactory(new PropertyValueFactory<>("valueAsString"));
		valueColumn.setCellFactory(tsTableColumn -> new CustomTableCell());
		valueColumn.setEditable(false);

		TableColumn<Parameter, Parameter> removeColumn = new TableColumn<>(R.EDIT_VARIABLE_DIALOG_DEL.get());
		removeColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue()));

		removeColumn.setCellFactory(param -> {
			TableCell<Parameter, Parameter> tableCell = new TableCell<Parameter, Parameter>() {
				private final Button deleteButton = new Button();

				@Override
				protected void updateItem(Parameter parameter, boolean empty) {
					super.updateItem(parameter, empty);

					if (parameter == null)
					{
						super.setGraphic(null);
						return;
					}
					deleteButton.setId("dictionaryBtnDeleteDialog");
					deleteButton.getStyleClass().add(CssVariables.TRANSPARENT_BACKGROUND);
					deleteButton.setOnAction(event -> tableView.getItems().remove(parameter));

					super.setGraphic(deleteButton);
				}
			};
			tableCell.setAlignment(Pos.CENTER);
			return tableCell;
		});

		tableView.getColumns().addAll(nameColumn, expressionColumn, valueColumn, removeColumn);
		tableView.getItems().addAll(parameters);

		BorderPane paneContainer = new BorderPane();
		paneContainer.setCenter(tableView);
		Button btnAddNewParameter = new Button(R.EDIT_VARIABLE_DIALOG_ADD_NEW.get());
		btnAddNewParameter.setOnAction(e ->
		{
			Parameter newParameter = new Parameter(R.EDIT_VARIABLE_DIALOG_PARAM_NAME.get(), R.EDIT_VARIABLE_DIALOG_EXPRESSION_VALUE.get());
			tableView.getItems().add(newParameter);
			newParameter.evaluate(evaluator);
		});
		paneContainer.setBottom(btnAddNewParameter);

		dialogPane.setContent(paneContainer);
		tableView.setOnKeyPressed(e ->
		{
			if (e.getCode() == KeyCode.ESCAPE)
			{
				super.close();
			}
		});
		super.setResultConverter(buttonType ->
		{
			if (buttonType == ButtonType.OK)
			{
				Parameters newParameters = new Parameters();
				newParameters.from(tableView.getItems());
				return newParameters;
			}
			return null;
		});
	}

	private void update(TableView<Parameter> tableView)
	{
		tableView.getColumns().forEach(column -> Common.runLater(() -> {
			column.setVisible(false);
			column.setVisible(true);
		}));
	}

	private class CustomTableCell extends TableCell<Parameter, String>
	{
		private TextField textField;

		CustomTableCell()
		{
			super();
			this.setAlignment(Pos.CENTER_LEFT);
		}

		@Override
		public void startEdit()
		{
			super.startEdit();
			if (super.getTableColumn().isEditable())
			{
				if (textField == null)
				{
					this.createTextField();
				}
				textField.setText(getString());
				super.setGraphic(textField);
				super.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Common.runLater(textField::requestFocus);
			}
		}

		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			super.setText(Str.asString(getItem()));
			super.setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override
		protected void updateItem(String s, boolean b)
		{
			super.updateItem(s, b);

			if (b || s == null)
			{
				super.setText(null);
				super.setGraphic(null);
			}
			else
			{
				super.setText(this.getString());
				super.setContentDisplay(ContentDisplay.TEXT_ONLY);
			}
		}

		private String getString()
		{
			return Str.asString(getItem());
		}

		private void createTextField()
		{
			textField = new TextField(getString());
			textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(t ->
			{
				if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
				{
					super.commitEdit(textField.getText());
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
			});
			textField.focusedProperty().addListener((observable, oldValue, newValue) ->
			{
				if (!newValue && textField != null)
				{
					super.commitEdit(textField.getText());
				}
			});
		}
	}
}
