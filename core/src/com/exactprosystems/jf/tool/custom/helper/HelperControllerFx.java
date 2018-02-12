////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.helper;

import com.exactprosystems.jf.api.app.Do;
import com.exactprosystems.jf.api.app.DoSpec;
import com.exactprosystems.jf.api.common.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.ContainingParent;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.custom.helper.HelperFx.IToString;
import com.exactprosystems.jf.tool.custom.FindListView;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.settings.Theme;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
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
import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;

public class HelperControllerFx implements Initializable, ContainingParent
{
	public BorderPane					borderPane;
	public TextArea						taDescription;
	public BorderPane mainPane;
	private Class<?>					clazz;

	private Dialog<ButtonType>          dialog;
	public  ToggleButton                btnVoid;
	public  ToggleButton                btnStatic;
	public  Button                      btnCancel;
	public  Button                      btnOk;
	//	public TextArea						styleClassedTextArea;
	public  StyleClassedTextArea        styleClassedTextArea;
	public  SplitMenuButton             smbClass;

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
		assert btnOk != null : "fx:id=\"btnOk\" was not injected: check your FXML file 'HelperFx.fxml'.";
		this.styleClassedTextArea = new StyleClassedTextArea();
		this.mainPane.setCenter(this.styleClassedTextArea);

		//				<TextArea fx:id="taInput" maxHeight="300.0" maxWidth="1.7976931348623157E308" prefHeight="200.0" prefWidth="200.0" BorderPane.alignment="CENTER"/>
		this.styleClassedTextArea.setMaxWidth(Double.MAX_VALUE);
		this.styleClassedTextArea.setMaxHeight(300.0);
		BorderPane.setAlignment(this.styleClassedTextArea, Pos.CENTER);
		this.styleClassedTextArea.setStyleSpans(0, Common.convertFromList(Highlighter.Java.getStyles(this.styleClassedTextArea.getText())));
		this.styleClassedTextArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> this.styleClassedTextArea.setStyleSpans(0, Common.convertFromList(Highlighter.Java.getStyles(this.styleClassedTextArea.getText()))));
		this.taResult.setFont(Font.font("Monospaced", 14));
		this.styleClassedTextArea.setWrapText(true);

		this.engine = viewClassName.getEngine();
		this.viewClassName.setContextMenuEnabled(false);
		this.smbClass.getItems().addAll(
				new MenuItem(DateTime.class.getSimpleName()),
				new MenuItem(Rnd.class.getSimpleName()),
				new MenuItem(Str.class.getSimpleName()),
				new MenuItem(Sys.class.getSimpleName()),
				new MenuItem(Do.class.getSimpleName()),
				new MenuItem(DoSpec.class.getSimpleName()),
				new MenuItem(Zip.class.getSimpleName()),
				new MenuItem(ProcessTools.class.getSimpleName())
		);
		this.smbClass.getItems().forEach(item -> item.setOnAction(event -> this.styleClassedTextArea.appendText(item.getText())));
	}

	@Override
	public void setParent(Parent parent)
	{
		this.dialog = new Alert(Alert.AlertType.CONFIRMATION);
		DialogsHelper.centreDialog(this.dialog);
		Common.addIcons(((Stage) this.dialog.getDialogPane().getScene().getWindow()));
		DialogPane dialogPane = this.dialog.getDialogPane();
		dialogPane.setPrefHeight(800);
		dialogPane.setPrefWidth(1000);
		dialogPane.setContent(parent);
		this.dialog.setResizable(true);
		this.dialog.setTitle(R.FORMULA_INTERPRETER.get());

	}

	// =====================================================================================
	// public
	// =====================================================================================
	public void init(HelperFx model, String title, Class<?> clazz, boolean editable)
	{
		this.model = model;
		this.clazz = clazz;
		this.styleClassedTextArea.setEditable(editable);
		this.listMembers = new FindListView<>((iToString, s) -> iToString.getName().toLowerCase().contains(s.toLowerCase()), false);
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
		this.dialog.setHeaderText(MessageFormat.format(R.PARAMETERS_NAME.get(), title == null ? "<none>" : title));
		listeners();
	}

	public String showAndWait(String expression)
	{
		this.styleClassedTextArea.appendText(expression == null ? "" : expression);
		this.dialog.getDialogPane().getScene().getStylesheets().addAll(Theme.currentThemesPaths());
		Optional<ButtonType> buttonType = this.dialog.showAndWait();
		return buttonType.isPresent() && buttonType.get().equals(ButtonType.OK) ?  styleClassedTextArea.getText() : expression;
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

	private void displayAnnotation(Class<?> clazz){
		//todo add more annotations for other types of classes
		DescriptionAttribute description = clazz.getAnnotation(DescriptionAttribute.class);
		if (description != null){
			this.taDescription.setText(description.text().get());
		} else {this.taDescription.setText("");}
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
		displayAnnotation(clazz);
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
		Common.tryCatch(() -> evaluate(getText()), R.HELPER_ERROR_VOID_METHODS.get());
	}

	public void staticVisible(ActionEvent event)
	{
		Common.tryCatch(() -> evaluate(getText()), R.HELPER_ERROR_STATIC_METHODS.get());
	}

	public void sorting(ActionEvent event)
	{
		Common.tryCatch(() -> evaluate(getText()), R.HELPER_ERROR_ON_SORTING_METHODS.get());
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
				int position = styleClassedTextArea.getCaretPosition();
				styleClassedTextArea.insertText(position, varName);
				styleClassedTextArea.moveTo(position + varName.length());
			});
		}, R.HELPER_ERROR_ON_ALL_VARS.get());
	}

	// ============================================================
	// private methods
	// ============================================================
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
		this.dialog.setOnShowing(event ->
		{
			Common.tryCatch(() ->
			{
				evaluate(styleClassedTextArea.getText());
				event.consume();
			}, R.HELPER_ERROR_ON_SHOWING.get());
		});

		this.styleClassedTextArea.textProperty().addListener((observableValue, before, after) -> Common.tryCatch(() -> evaluate(after), R.HELPER_ERROR_ON_EVALUATE.get()));

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
						int caretPosition = styleClassedTextArea.getCaretPosition();
						String methodName = method.getName();
						if (styleClassedTextArea.getText().indexOf(".", caretPosition - 1) != (caretPosition - 1))
						{
							styleClassedTextArea.insertText(caretPosition, ".");
							caretPosition++;
						}
						styleClassedTextArea.insertText(caretPosition, method.getMethodWithParams());
						styleClassedTextArea.moveTo(caretPosition + methodName.length() + 2);
					}
					else if (selectedMember instanceof HelperFx.SimpleField)
					{
						HelperFx.SimpleField field = ((HelperFx.SimpleField) selectedMember);
						int caretPosition = styleClassedTextArea.getCaretPosition();
						String methodName = field.getName();
						if (styleClassedTextArea.getText().indexOf(".", caretPosition - 1) != (caretPosition - 1))
						{
							styleClassedTextArea.insertText(caretPosition, ".");
							caretPosition++;
						}
						styleClassedTextArea.insertText(caretPosition, methodName);
						styleClassedTextArea.moveTo(caretPosition + methodName.length());
					}
				}
			}
		}, R.HELPER_ERROR_ON_FUNCTION_INSERT.get()));

		styleClassedTextArea.selectedTextProperty().addListener((observableValue, before, after) ->
		{
			Common.tryCatch(() ->
			{
				if (after.isEmpty())
				{
					evaluate(styleClassedTextArea.getText());
				}
				else
				{
					evaluate(after);
				}
			}, R.HELPER_ERROR_ON_EVALUATE.get());
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
			DialogsHelper.centreDialog(dialog);
			Common.addIcons(((Stage) dialog.getDialogPane().getScene().getWindow()));
			dialog.getDialogPane().setContent(tableView);
			dialog.getDialogPane().setPrefWidth(500);
			dialog.getDialogPane().setPrefHeight(500);
			dialog.setResizable(true);
			dialog.setHeaderText(R.ALL_VARIABLES_FOR_CURRENT_MATRIX.get());

			tableView.setOnMouseClicked(mouseEvent -> {
				Common.tryCatch(() -> {
					if (mouseEvent.getClickCount() == 2)
					{
						Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
						dialog.close();
					}
				}, R.HELPER_ERROR_ON_CLICK.get());
			});

			tableView.setOnKeyPressed(keyEvent -> {
				Common.tryCatch(() -> {
					if (keyEvent.getCode() == KeyCode.ENTER)
					{
						Optional.ofNullable(tableView.getSelectionModel().getSelectedItem()).ifPresent(i -> name = i.getName());
						dialog.close();
					}
				}, R.HELPER_ERROR_ON_KEY_PRESSED.get());
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
			nameColumn.setText(R.COMMON_SHIFT_NAME.get());
			nameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
			final TableColumn<SimpleVariable, String> clazzColumn = new TableColumn<>();
			clazzColumn.setText(R.COMMON_CLASS.get());
			clazzColumn.setCellValueFactory(new PropertyValueFactory<>("clazz"));

			final TableColumn<SimpleVariable, String> expressionColumn = new TableColumn<>();
			expressionColumn.setText(R.COMMON_VALUE.get());
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