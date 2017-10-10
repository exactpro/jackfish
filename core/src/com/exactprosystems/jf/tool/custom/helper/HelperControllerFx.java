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
import com.exactprosystems.jf.api.common.*;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.helper.HelperFx.IToString;
import com.exactprosystems.jf.tool.dictionary.FindListView;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Font;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import org.fxmisc.richtext.StyleClassedTextArea;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class HelperControllerFx implements Initializable, ContainingParent
{
	@FXML
	private BorderPane           borderPane;
	@FXML
	private TextArea             taDescription;
	@FXML
	private BorderPane           mainPane;
	private Dialog<ButtonType>   dialog;
	@FXML
	private ToggleButton         btnVoid;
	@FXML
	private ToggleButton         btnStatic;
	@FXML
	private StyleClassedTextArea styleClassedTextArea;
	@FXML
	private SplitMenuButton      smbClass;
	@FXML
	private TextArea             taResult;
	@FXML
	private WebView              viewClassName;
	@FXML
	private ToggleButton         btnSorting;
	@FXML
	private Button               btnAllVars;

	private WebEngine               engine;
	private HelperFx                model;
	private Class<?>                clazz;
	private FindListView<IToString> listMembers;


	@Override
	public void initialize(URL url, ResourceBundle resourceBundle)
	{
		assert listMembers != null : "fx:id=\"listViewMethods\" was not injected: check your FXML file 'HelperFx.fxml'.";

		this.styleClassedTextArea = new StyleClassedTextArea();
		this.styleClassedTextArea.setMaxWidth(Double.MAX_VALUE);
		this.styleClassedTextArea.setMaxHeight(300.0);
		BorderPane.setAlignment(this.styleClassedTextArea, Pos.CENTER);
		this.mainPane.setCenter(this.styleClassedTextArea);
		this.styleClassedTextArea.setStyleSpans(0, Common.convertFromList(Highlighter.Java.getStyles(this.styleClassedTextArea.getText())));
		this.styleClassedTextArea.richChanges().filter(ch -> !ch.getInserted().equals(ch.getRemoved())).subscribe(
				change -> this.styleClassedTextArea.setStyleSpans(0, Common.convertFromList(Highlighter.Java.getStyles(this.styleClassedTextArea.getText()))));
		this.styleClassedTextArea.setWrapText(true);

		this.taResult.setFont(Font.font("Monospaced", 14));

		this.engine = this.viewClassName.getEngine();
		this.viewClassName.setContextMenuEnabled(false);
		this.smbClass.getItems().addAll(new MenuItem(DateTime.class.getSimpleName()), new MenuItem(Rnd.class.getSimpleName()), new MenuItem(Str.class.getSimpleName()),
				new MenuItem(Sys.class.getSimpleName()), new MenuItem(Do.class.getSimpleName()), new MenuItem(DoSpec.class.getSimpleName()));
		this.smbClass.getItems().forEach(item -> item.setOnAction(event -> this.styleClassedTextArea.appendText(item.getText())));
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
		this.btnAllVars.setDisable(!editable);
		this.styleClassedTextArea.setEditable(editable);
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
		this.listMembers.addChangeListener((observable, oldValue, newValue) -> {
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
		this.styleClassedTextArea.appendText(expression == null ? "" : expression);
		this.dialog.getDialogPane().getScene().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = this.dialog.showAndWait();
		return buttonType.isPresent() && buttonType.get().equals(ButtonType.OK) ? this.styleClassedTextArea.getText() : expression;
	}

	public void close()
	{
		this.dialog.close();
	}

	void compileFailed(String text)
	{
		this.displayResult(text, CssVariables.COMPILE_FAILED);
	}

	void evaluateFailed(String text)
	{
		this.displayResult(text, CssVariables.EVALUATE_FAILED);
	}

	void successEvaluate(String text)
	{
		this.displayResult(text, CssVariables.EVALUATE_SUCCESS);
	}

	void displayClass(Class<?> clazz)
	{
		StringBuilder className = new StringBuilder("<html><body bgcolor='#f5f5dc'>");
		className.append("<label style='display:").append(clazz.isArray() ? "none" : "inline").append("'>").append("<font color='#d2691e'>class </font></label>");
		className.append("<label><font color='black'>").append(clazz.getSimpleName()).append(" </font></label>");
		Class<?> superClass = clazz.getSuperclass();
		boolean var = superClass == Object.class;
		className.append("<label style='display:").append(var ? "none" : "inline").append("'>").append("<font color='#d2691e'>extends </font></label>");
		className.append("<label style='display:").append(var ? "none" : "inline").append("'>").append("<font color='black'>").append(superClass.getSimpleName()).append(" </font></label>");
		className.append("</body></html>");
		this.engine.loadContent(className.toString());
		displayAnnotation(clazz);
	}

	void displayMethods(ObservableList<HelperFx.IToString> list)
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
		this.evaluate(getText());
	}

	public void staticVisible(ActionEvent event)
	{
		this.evaluate(getText());
	}

	public void sorting(ActionEvent event)
	{
		this.evaluate(getText());
	}

	public void showAllVars(ActionEvent event)
	{
		ObservableList<SimpleVariable> data = FXCollections.observableArrayList();
		this.model.fillVariables(data);
		ShowAllVars vars = new ShowAllVars(data, this.clazz);
		Optional.ofNullable(vars.showAndGet()).ifPresent(varName -> {
			int position = styleClassedTextArea.getCaretPosition();
			this.styleClassedTextArea.insertText(position, varName);
			this.styleClassedTextArea.moveTo(position + varName.length());
		});
	}

	//region private methods
	private void displayAnnotation(Class<?> clazz)
	{
		//todo add more annotations for other types of classes
		DescriptionAttribute description = clazz.getAnnotation(DescriptionAttribute.class);
		if (description != null)
		{
			this.taDescription.setText(description.text());
		}
		else
		{
			this.taDescription.setText("");
		}
	}

	private void displayResult(String text, String styleClass)
	{
		this.taResult.setText(text);
		this.taResult.getStyleClass().removeAll(this.taResult.getStyleClass());
		this.taResult.getStyleClass().add(styleClass);
	}

	private String getText()
	{
		if (this.styleClassedTextArea.getSelectedText().isEmpty())
		{
			return this.styleClassedTextArea.getText();
		}
		else
		{
			return this.styleClassedTextArea.getSelectedText();
		}
	}

	private void listeners()
	{
		this.dialog.setOnShowing(event -> evaluate(this.styleClassedTextArea.getText()));

		this.styleClassedTextArea.textProperty().addListener((observableValue, oldValue, newValue) -> evaluate(newValue));

		this.listMembers.getListView().setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getClickCount() == 2)
			{
				IToString selectedMember = this.listMembers.getSelectedItem();

				if (selectedMember != null)
				{
					if (selectedMember instanceof HelperFx.SimpleMethod)
					{
						HelperFx.SimpleMethod method = (HelperFx.SimpleMethod) selectedMember;
						int caretPosition = this.styleClassedTextArea.getCaretPosition();
						String methodName = method.getName();
						if (this.styleClassedTextArea.getText().indexOf('.', caretPosition - 1) != (caretPosition - 1))
						{
							this.styleClassedTextArea.insertText(caretPosition, ".");
							caretPosition++;
						}
						this.styleClassedTextArea.insertText(caretPosition, method.getMethodWithParams());
						this.styleClassedTextArea.moveTo(caretPosition + methodName.length() + 2);
					}
					else if (selectedMember instanceof HelperFx.SimpleField)
					{
						HelperFx.SimpleField field = ((HelperFx.SimpleField) selectedMember);
						int caretPosition = this.styleClassedTextArea.getCaretPosition();
						String methodName = field.getName();
						if (this.styleClassedTextArea.getText().indexOf('.', caretPosition - 1) != (caretPosition - 1))
						{
							this.styleClassedTextArea.insertText(caretPosition, ".");
							caretPosition++;
						}
						this.styleClassedTextArea.insertText(caretPosition, methodName);
						this.styleClassedTextArea.moveTo(caretPosition + methodName.length());
					}
				}
			}
		});

		this.styleClassedTextArea.selectedTextProperty().addListener((observableValue, oldValue, newValue) -> evaluateFailed(newValue.isEmpty() ? this.styleClassedTextArea.getText() : newValue));
	}

	private void evaluate(String expression)
	{
		this.model.evaluate(expression, !this.btnVoid.isSelected(), !this.btnStatic.isSelected(), !this.btnSorting.isSelected());
	}

	//endregion

	private class ShowAllVars
	{
		private String name = null;
		private TableView<SimpleVariable> tableView;

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
				if (mouseEvent.getClickCount() == 2)
				{
					Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
					dialog.close();
				}
			});

			tableView.setOnKeyPressed(keyEvent -> {
				if (keyEvent.getCode() == KeyCode.ENTER)
				{
					Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
					dialog.close();
				}
			});
			dialog.getDialogPane().getStylesheets().addAll(Theme.currentThemesPaths());
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

	private static class ColorRow extends TableRow<SimpleVariable>
	{
		private Class<?> expectedClazz;

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