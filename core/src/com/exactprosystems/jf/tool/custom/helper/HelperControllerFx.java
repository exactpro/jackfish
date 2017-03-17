////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.helper;

import com.exactprosystems.jf.api.app.Do;
import com.exactprosystems.jf.api.app.DoSpec;
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.api.common.Rnd;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.helper.HelperFx.IToString;
import com.exactprosystems.jf.tool.dictionary.FindListView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class HelperControllerFx implements Initializable, ContainingParent
{
	public BorderPane					borderPane;
	public TextArea						taDescription;
	private Class<?>					clazz;

	private Dialog<ButtonType>			dialog;
	public ToggleButton					btnVoid;
	public ToggleButton					btnStatic;
	public Button						btnCancel;
	public Button						btnOk;
	public TextArea						taInput;
	public SplitMenuButton				smbClass;

	private FindListView<IToString>		listMembers;
	public TextArea						taResult;
	public WebView						viewClassName;
	public ToggleButton					btnSorting;
	public Button						btnAllVars;

	private WebEngine					engine;
	private HelperFx					model;

	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert listMembers != null : "fx:id=\"listViewMethods\" was not injected: check your FXML file 'HelperFx.fxml'.";
		assert btnCancel != null : "fx:id=\"btnCancel\" was not injected: check your FXML file 'HelperFx.fxml'.";
		assert taInput != null : "fx:id=\"taInput\" was not injected: check your FXML file 'HelperFx.fxml'.";
		assert btnOk != null : "fx:id=\"btnOk\" was not injected: check your FXML file 'HelperFx.fxml'.";
		this.taResult.setFont(Font.font("Monospaced", 14));
		this.taInput.setWrapText(true);

		this.engine = viewClassName.getEngine();
		this.viewClassName.setContextMenuEnabled(false);
		this.smbClass.getItems().addAll(
				new MenuItem(DateTime.class.getSimpleName()),
				new MenuItem(Rnd.class.getSimpleName()),
				new MenuItem(Str.class.getSimpleName()),
                new MenuItem(Sys.class.getSimpleName()),
				new MenuItem(Do.class.getSimpleName()),
				new MenuItem(DoSpec.class.getSimpleName())
		);
		this.smbClass.getItems().forEach(item -> item.setOnAction(event -> this.taInput.appendText(item.getText())));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.dialog = new Alert(Alert.AlertType.CONFIRMATION);
		Common.addIcons(((Stage) this.dialog.getDialogPane().getScene().getWindow()));
		DialogPane dialogPane = this.dialog.getDialogPane();
		dialogPane.setPrefHeight(800);
		dialogPane.setPrefWidth(1000);
		dialogPane.setContent(parent);
		this.dialog.setResizable(true);
		this.dialog.setTitle("Formula interpreter");

	}

	// =====================================================================================
	// public
	// =====================================================================================
	public void init(HelperFx model, String title, Class<?> clazz, boolean editable)
	{
		this.model = model;
		this.clazz = clazz;
		this.taInput.setEditable(editable);
		this.listMembers = new FindListView<>((iToString, s) -> iToString.getName().contains(s), false);
		this.borderPane.setCenter(this.listMembers);

		this.listMembers.setCellFactory(list -> new ListCell<IToString>()
		{
			@Override
			public void updateItem(IToString item, boolean empty)
			{
				super.updateItem(item, empty);
				if (empty)
				{
					setText(null);
					setGraphic(null);
				}
				else
				{
					setText(item.toString());
				}
			}
		});
		this.listMembers.addChangeListener((observable, oldValue, newValue) ->
		{
			if (newValue != null)
			{
				String description = newValue.getDescription();
				taDescription.setText(description == null ? "" : description);
			}
			else
			{
				taDescription.setText("");
			}
		});
		this.dialog.setHeaderText("Parameters name = " + (title == null ? "<none>" : title));
		listeners();
	}

	public String showAndWait(String expression)
	{
		this.taInput.setText(expression);
		this.dialog.getDialogPane().getScene().getStylesheets().addAll(Common.currentThemesPaths());
		Optional<ButtonType> buttonType = this.dialog.showAndWait();
		return buttonType.isPresent() && buttonType.get().equals(ButtonType.OK) ?  taInput.getText() : expression;
	}

	public void close()
	{
		this.dialog.close();
	}

	public void compileFailed(String text)
	{
		this.displayResult(text, CssVariables.COMPILE_FAILED);
	}

	public void evaluateFailed(String text)
	{
		this.displayResult(text, CssVariables.EVALUATE_FAILED);
	}

	public void successEvaluate(String text)
	{
		this.displayResult(text, CssVariables.EVALUATE_SUCCESS);
	}

	private void displayResult(String text, String styleClass)
	{
		this.taResult.setText(text);
		this.taResult.getStyleClass().removeAll(this.taResult.getStyleClass());
		this.taResult.getStyleClass().add(styleClass);
	}

	public void displayClass(Class<?> clazz)
	{
		StringBuilder className = new StringBuilder("<html><body bgcolor='#f5f5dc'>");
		className.append("<label style='display:").append(clazz.isArray() ? "none" : "inline").append("'>")
				.append("<font color='#d2691e'>class </font></label>");
		className.append("<label><font color='black'>").append(clazz.getSimpleName()).append(" </font></label>");
		String superClassName = clazz.getSuperclass().getSimpleName();
		boolean var = superClassName.equals(Object.class.getSimpleName());
		className.append("<label style='display:").append(var ? "none" : "inline").append("'>").append("<font color='#d2691e'>extends </font></label>");
		className.append("<label style='display:").append(var ? "none" : "inline").append("'>").append("<font color='black'>").append(superClassName)
				.append(" </font></label>");
		className.append("</body></html>");
		engine.loadContent(className.toString());
	}

	public void displayMethods(ObservableList<HelperFx.IToString> list)
	{
		this.listMembers.setData(list, true);
		this.listMembers.getItems().clear();
		this.listMembers.getItems().addAll(list);
	}

	// ============================================================
	// events methods
	// ============================================================
	public void voidVisible(ActionEvent event)
	{
		Common.tryCatch(() -> evaluate(getText()), "Error on change visible void methods");
	}

	public void staticVisible(ActionEvent event)
	{
		Common.tryCatch(() -> evaluate(getText()), "Error on change visible static methods");
	}

	public void sorting(ActionEvent event)
	{
		Common.tryCatch(() -> evaluate(getText()), "Error on sorting methods");
	}

	public void showAllVars(ActionEvent event)
	{
		Common.tryCatch(() ->
		{
			ObservableList<SimpleVariable> data = FXCollections.observableArrayList();
			model.fillVariables(data);

			ShowAllVars vars = new ShowAllVars(data, this.clazz);
			Optional.ofNullable(vars.showAndGet()).ifPresent(varName ->
			{
				int position = taInput.getCaretPosition();
				taInput.insertText(position, varName);
				taInput.positionCaret(position + varName.length());
			});
		}, "Error on show all vars");
	}

	// ============================================================
	// private methods
	// ============================================================
	private String getText()
	{
		if (this.taInput.getSelectedText().isEmpty())
		{
			return this.taInput.getText();
		}
		else
		{
			return this.taInput.getSelectedText();
		}
	}

	private void listeners()
	{
		this.dialog.setOnShowing(event ->
		{
			Common.tryCatch(() ->
			{
				evaluate(taInput.getText());
				event.consume();
			}, "Error on showing");
		});

		this.taInput.textProperty().addListener((observableValue, before, after) -> Common.tryCatch(() -> evaluate(after), "Error on evaluate"));

		this.listMembers.getListView().setOnMouseClicked(mouseEvent -> Common.tryCatch(() ->
		{
			if (mouseEvent.getClickCount() == 2)
			{
				IToString selectedMember = listMembers.getSelectedItem();

				if (selectedMember != null)
				{
					if (selectedMember instanceof HelperFx.SimpleMethod)
					{
						HelperFx.SimpleMethod method = (HelperFx.SimpleMethod) selectedMember;
						int caretPosition = taInput.getCaretPosition();
						String methodName = method.getName();
						if (taInput.getText().indexOf(".", caretPosition - 1) != (caretPosition - 1))
						{
							taInput.insertText(caretPosition, ".");
							caretPosition++;
						}
						taInput.insertText(caretPosition, method.getMethodWithParams());
						taInput.positionCaret(caretPosition + methodName.length() + 2);
					}
					else if (selectedMember instanceof HelperFx.SimpleField)
					{
						HelperFx.SimpleField field = ((HelperFx.SimpleField) selectedMember);
						int caretPosition = taInput.getCaretPosition();
						String methodName = field.getName();
						if (taInput.getText().indexOf(".", caretPosition - 1) != (caretPosition - 1))
						{
							taInput.insertText(caretPosition, ".");
							caretPosition++;
						}
						taInput.insertText(caretPosition, methodName);
						taInput.positionCaret(caretPosition + methodName.length());
					}
				}
			}
		}, "Error on function insertion"));

		taInput.selectedTextProperty().addListener((observableValue, before, after) ->
		{
			Common.tryCatch(() ->
			{
				if (after.isEmpty())
				{
					evaluate(taInput.getText());
				}
				else
				{
					evaluate(after);
				}
			}, "Error on evaluate");
		});
	}

	private void evaluate(String expression)
	{
		this.model.evaluate(expression, !this.btnVoid.isSelected(), !this.btnStatic.isSelected(), !this.btnSorting.isSelected());
	}

	private class ShowAllVars
	{
		private String						name	= null;
		private TableView<SimpleVariable>	tableView;

		public ShowAllVars(ObservableList<SimpleVariable> data, Class<?> clazz)
		{
			this.tableView = createTable(clazz);
			tableView.setItems(data);
		}

		public String showAndGet()
		{
			Dialog<ButtonType> dialog = new Alert(Alert.AlertType.CONFIRMATION);
			Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
			dialog.getDialogPane().setContent(tableView);
			dialog.getDialogPane().setPrefWidth(500);
			dialog.getDialogPane().setPrefHeight(500);
			dialog.setResizable(true);
			dialog.setHeaderText("All variables for current matrix");

			tableView.setOnMouseClicked(mouseEvent -> {
				Common.tryCatch(() -> {
					if (mouseEvent.getClickCount() == 2)
					{
						Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
						dialog.close();
					}
				}, "Error on click");
			});

			tableView.setOnKeyPressed(keyEvent -> {
				Common.tryCatch(() -> {
					if (keyEvent.getCode() == KeyCode.ENTER)
					{
						Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
						dialog.close();
					}
				}, "Error on key pressed");
			});
			dialog.getDialogPane().getStylesheets().addAll(Common.currentThemesPaths());
			Optional<ButtonType> optional = dialog.showAndWait();
			optional.filter(buttonType -> buttonType.getButtonData().equals(ButtonBar.ButtonData.OK_DONE)).ifPresent(
					type -> Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName()));
			return this.name;
		}

		private TableView<SimpleVariable> createTable(final Class<?> clazz)
		{
			TableView<SimpleVariable> tableView = new TableView<>();
			tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

			final TableColumn<SimpleVariable, String> nameColumn = new TableColumn<>();
			nameColumn.setText("Name");
			nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
			final TableColumn<SimpleVariable, String> clazzColumn = new TableColumn<>();
			clazzColumn.setText("Class");
			clazzColumn.setCellValueFactory(new PropertyValueFactory<>("clazz"));

			final TableColumn<SimpleVariable, String> expressionColumn = new TableColumn<>();
			expressionColumn.setText("Value");
			expressionColumn.setCellValueFactory(new PropertyValueFactory<>("value"));

			tableView.getColumns().addAll(nameColumn, clazzColumn, expressionColumn);
			tableView.setRowFactory(tableView1 -> new ColorRow(clazz));

			return tableView;
		}
	}

	private class ColorRow extends TableRow<SimpleVariable>
	{
		private Class<?>	expectedClazz;

		public ColorRow(Class<?> expectedClazz)
		{
			this.expectedClazz = expectedClazz;
		}

		@Override
		protected void updateItem(SimpleVariable e, boolean b)
		{
			super.updateItem(e, b);
			if (e == null)
			{
				return;
			}
			if (expectedClazz != null && e.getClazz() != null && e.getClazz().equals(expectedClazz.getSimpleName()))
			{
				this.getStyleClass().addAll(CssVariables.EXPECTED_CLASS);
			}
		}
	}

}