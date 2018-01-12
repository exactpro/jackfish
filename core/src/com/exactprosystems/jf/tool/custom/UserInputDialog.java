////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.Converter;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.custom.date.DateTimePicker;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import com.sun.javafx.scene.control.skin.DatePickerSkin;
import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.util.Duration;

import java.io.File;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Supplier;

public class UserInputDialog extends Dialog<Object>
{
	private Control  mainControl;
	private Timeline timer;
	private Supplier<Object> result = () -> null;

	public UserInputDialog(Object defaultValue, AbstractEvaluator evaluator, HelpKind helpKind, List<ReadableValue> dataSource, int timeout)
	{
		DialogPane dialogPane = getDialogPane();
		this.setResizable(true);
		dialogPane.getStylesheets().addAll(Theme.currentThemesPaths());
		this.timer = new Timeline(new KeyFrame(Duration.millis(timeout < 0 ? Integer.MAX_VALUE : timeout), ae -> done()));
		this.timer.setCycleCount(1);
		this.timer.play();

		try
		{
			switch (helpKind)
			{
				case Number:
					this.mainControl = createNumberTextField(Converter.convertToType(defaultValue, Integer.class));
					break;

				case Boolean:
					this.mainControl = createBooleanField(Converter.convertToType(defaultValue, Boolean.class));
					break;

				case Expression:
					this.mainControl = createExpressionField(evaluator, Str.asString(defaultValue));
					break;

				case ChooseDateTime:
					this.mainControl = createDateTimePickerField(Converter.convertToType(defaultValue, Date.class));
					break;

				case ChooseOpenFile:
					ExpressionField openFile = createExpressionField(evaluator, Str.asString(defaultValue));
					this.mainControl = openFile;
					openFile.setNameSecond(helpKind.getLabel());
					openFile.setSecondActionListener(str -> {
						this.timer.stop();
						File file = DialogsHelper.showOpenSaveDialog(R.USER_INPUT_DIALOG_CHOOSE_FILE.get(), R.COMMON_ALL_FILES.get(), "*.*", DialogsHelper.OpenSaveMode.OpenFile);
						this.timer.play();
						if (file != null)
						{
							return Common.getRelativePath(file.getAbsolutePath());
						}
						return str;
					});
					break;

				case ChooseFolder:
					ExpressionField chooseFolder = createExpressionField(evaluator, Str.asString(defaultValue));
					this.mainControl = chooseFolder;
					chooseFolder.setNameSecond(helpKind.getLabel());
					chooseFolder.setSecondActionListener(str -> {
						this.timer.stop();
						File file = DialogsHelper.showDirChooseDialog(R.USER_INPUT_DIALOG_CHOOSE_DIR.get());
						this.timer.play();
						if (file != null)
						{
							return Common.getRelativePath(file.getAbsolutePath());
						}
						return str;
					});
					break;

				case ChooseFromList:
					this.mainControl = createListViewField(dataSource == null ? Collections.emptyList() : dataSource, Str.asString(defaultValue));
					break;

				default:
					this.mainControl = createTextField(Str.asString(defaultValue));
					break;
			}

			EventHandler<? super KeyEvent> onKeyPressed = this.mainControl.getOnKeyPressed();
			this.mainControl.setOnKeyPressed(k -> {
				if (onKeyPressed != null)
				{
					onKeyPressed.handle(k);
				}
				restartTimer();
			});

			EventHandler<? super MouseEvent> onMouseMoved = this.mainControl.getOnMouseMoved();
			this.mainControl.setOnMouseMoved(m -> {
				if (onMouseMoved != null)
				{
					onMouseMoved.handle(m);
				}
				restartTimer();
			});
		}
		catch (Exception ignored)
		{}

		GridPane grid = new GridPane();
		grid.setHgap(10);
		grid.setVgap(10);
		grid.setMaxWidth(Double.MAX_VALUE);
		grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		dialogPane.setPrefWidth(550);
		dialogPane.setMaxWidth(550);
		dialogPane.setMinWidth(550);
		dialogPane.setPrefHeight(200);
		dialogPane.setMinHeight(200);
		dialogPane.setMaxHeight(200);
		dialogPane.setContent(grid);

		setResultConverter(dialogButton -> {
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			this.timer.stop();
			return data == ButtonBar.ButtonData.OK_DONE ? this.result.get() : null;
		});

		if (this.mainControl == null)
		{
			Common.runLater(this::close);
			return;
		}

		grid.getChildren().clear();
		grid.add(this.mainControl, 0, 0);

		Common.runLater(this.mainControl::requestFocus);
	}

	//region private methods
	private DateTimePicker createDateTimePickerField(Date defaultValue)
	{
		DateTimePicker field = new DateTimePicker(defaultValue);
		field.getEditor().setOnMouseMoved(m -> restartTimer());
		field.getEditor().setOnKeyPressed(m -> restartTimer());
		field.setOnShown(e -> {
			DatePickerSkin skin = ((DatePickerSkin) field.getSkin());
			skin.getPopupContent().setOnMouseMoved(m -> restartTimer());
			skin.getPopupContent().setOnKeyPressed(m -> restartTimer());
		});
		field.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(field, Priority.ALWAYS);
		GridPane.setFillWidth(field, true);
		this.result = field::getDate;
		return field;
	}

	private CheckBox createBooleanField(boolean defaultValue)
	{
		CheckBox field = new CheckBox();
		field.setIndeterminate(false);
		field.setSelected(defaultValue);
		field.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(field, Priority.ALWAYS);
		GridPane.setFillWidth(field, true);
		this.result = field::isSelected;

		return field;
	}

	private NumberTextField createNumberTextField(int defaultValue)
	{
		NumberTextField field = new NumberTextField(defaultValue);
		field.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(field, Priority.ALWAYS);
		GridPane.setFillWidth(field, true);
		this.result = field::getValue;

		return field;
	}

	private TextField createTextField(String defaultValue)
	{
		TextField field = new TextField(defaultValue);
		field.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(field, Priority.ALWAYS);
		GridPane.setFillWidth(field, true);
		this.result = field::getText;

		return field;
	}

	private ExpressionField createExpressionField(AbstractEvaluator evaluator, String defaultExpression)
	{
		ExpressionField expressionField = new ExpressionField(evaluator, defaultExpression);
		expressionField.setHelperForExpressionField(R.USER_INPUT_DIALOG_TITLE.get(), null);
		expressionField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(expressionField, Priority.ALWAYS);
		GridPane.setFillWidth(expressionField, true);
		this.result = expressionField::getText;

		return expressionField;
	}

	private ListView<ReadableValue> createListViewField(List<ReadableValue> dataSource, String defaultValue)
	{
		ListView<ReadableValue> list = new ListView<>(FXCollections.observableList(dataSource));
		list.getSelectionModel().select(new ReadableValue(defaultValue));
		list.setOnKeyPressed(keyEvent ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				done();
			}
		});
		list.setOnMouseClicked(mouseEvent ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				done();
			}
		});
		list.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(list, Priority.ALWAYS);
		GridPane.setFillWidth(list, true);
		this.result = () -> {
			ReadableValue item = list.getSelectionModel().getSelectedItem();
			return item == null ? null : item.getValue();
		};

		return list;
	}

	private void done()
	{
		Button buttonOk = ((Button) getDialogPane().lookupButton(ButtonType.OK));
		Common.runLater(() -> {
			if (buttonOk != null)
			{
				buttonOk.requestFocus();
				buttonOk.fire();
			}
		});
	}

	private void restartTimer()
	{
		if (this.timer.getStatus() != Status.STOPPED)
		{
			this.timer.stop();
			this.timer.play();
		}
	}
	//endregion
}
