package com.exactprosystems.jf.tool.custom;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

public class UserInputDialog extends Dialog<String>
{
	private final GridPane grid;
	private final ExpressionField expressionField;
	private final Label expressionLabel;
	private AbstractEvaluator evaluator;

	public UserInputDialog(String defaultValue, AbstractEvaluator evaluator, HelpKind helpKind, Collection<?> dataSource)
	{
		final DialogPane dialogPane = getDialogPane();
		this.setResizable(true);
		dialogPane.getStylesheets().addAll(Common.currentThemesPaths());
		this.evaluator = evaluator;
		this.expressionField = new ExpressionField(evaluator, defaultValue);
		this.expressionLabel = new Label();
		this.expressionField.setHelperForExpressionField("Title", null);
		if (helpKind != null )
		{
			switch (helpKind)
			{
				case ChooseDateTime:
					expressionField.setNameFirst("D");
					expressionField.setFirstActionListener(str ->
					{
						Date date = null;
						if (expressionField.getText() != null)
						{
							try
							{
								date = (Date) evaluator.evaluate(expressionField.getText());
							}
							catch (Exception e)
							{
								date = DateTime.current();
							}
						}


						Date res = DialogsHelper.showDateTimePicker(date);
						if (res != null)
						{
							LocalDateTime ldt = Common.convert(res);
							return String.format("DateTime.date(%d, %d, %d,  %d, %d, %d)",
									//				because localDateTime begin month from 1, not 0
									ldt.getYear(), ldt.getMonthValue() - 1, ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond());
						}
						return expressionField.getText();
					});
					break;

				case ChooseOpenFile:
					expressionField.setNameFirst("…");
					expressionField.setFirstActionListener(str ->
					{
						File file = DialogsHelper.showOpenSaveDialog("Choose file to open", "All files", "*.*", DialogsHelper.OpenSaveMode.OpenFile);
						if (file != null)
						{
							return evaluator.createString(Common.getRelativePath(file.getAbsolutePath()));
						}
						return str;
					});
					break;

				case ChooseSaveFile:
					expressionField.setNameFirst("…");
					expressionField.setFirstActionListener(str ->
					{
						File file = DialogsHelper.showOpenSaveDialog("Choose file to save", "All files", "*.*", DialogsHelper.OpenSaveMode.SaveFile);
						if (file != null)
						{
							return evaluator.createString(Common.getRelativePath(file.getAbsolutePath()));
						}
						return str;
					});
					break;

				case ChooseFolder:
					expressionField.setNameFirst("…");
					expressionField.setFirstActionListener(str ->
					{
						File file = DialogsHelper.showDirChooseDialog("Choose directory");
						if (file != null)
						{
							return evaluator.createString(Common.getRelativePath(file.getAbsolutePath()));
						}
						return str;
					});
					break;

				case ChooseFromList:
					expressionField.setChooserForExpressionField("Choose", () ->
							dataSource.stream()
									.map(ds -> new ReadableValue(Common.createLiteral(ds, evaluator)))
									.collect(Collectors.toList()));
					break;

				default:
					break;
			}
		}
		this.expressionField.setMaxWidth(Double.MAX_VALUE);
		GridPane.setHgrow(expressionField, Priority.ALWAYS);
		GridPane.setFillWidth(expressionField, true);

		GridPane.setHgrow(expressionLabel, Priority.ALWAYS);
		GridPane.setFillWidth(expressionLabel, true);

		this.grid = new GridPane();
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setMaxWidth(Double.MAX_VALUE);
		this.grid.setAlignment(Pos.CENTER_LEFT);

		dialogPane.contentTextProperty().addListener(o -> updateGrid());
		dialogPane.getButtonTypes().addAll(ButtonType.OK);
		dialogPane.setPrefWidth(300);
		dialogPane.setMaxWidth(300);
		dialogPane.setMinWidth(300);

		updateGrid();
		updateExpressionLabel(defaultValue);
		this.expressionField.textProperty().addListener((observable, oldValue, newValue) -> {
			updateExpressionLabel(newValue);
		});

		setResultConverter((dialogButton) ->
		{
			ButtonBar.ButtonData data = dialogButton == null ? null : dialogButton.getButtonData();
			return data == ButtonBar.ButtonData.OK_DONE ? expressionField.getText() : null;
		});
	}

	private void updateExpressionLabel(String value)
	{
		this.expressionLabel.getStyleClass().removeAll(CssVariables.INCORRECT_FIELD, CssVariables.EVALUATE_SUCCESS);
		try
		{
			this.expressionLabel.setText(String.valueOf(evaluator.evaluate(value)));
			this.expressionLabel.getStyleClass().add(CssVariables.EVALUATE_SUCCESS);
		}
		catch (Exception e)
		{
			this.expressionLabel.getStyleClass().add(CssVariables.INCORRECT_FIELD);
			this.expressionLabel.setText(e.getMessage());
		}
	}

	private void updateGrid()
	{
		grid.getChildren().clear();

		grid.add(expressionField, 0, 0);
		grid.add(expressionLabel, 0, 1);
		getDialogPane().setContent(grid);

		Platform.runLater(expressionField::requestFocus);
	}
}
