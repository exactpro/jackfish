////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.functions.Table;
import com.exactprosystems.jf.functions.Text;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.DragDetector;
import com.exactprosystems.jf.tool.custom.controls.field.autocomplete.AutoCompletionTextFieldBinding;
import com.exactprosystems.jf.tool.custom.controls.field.autocomplete.SuggestionProvider;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.grideditor.DataProvider;
import com.exactprosystems.jf.tool.custom.grideditor.SpreadsheetView;
import com.exactprosystems.jf.tool.custom.grideditor.TableDataProvider;
import com.exactprosystems.jf.tool.custom.label.CommentsLabel;
import com.exactprosystems.jf.tool.custom.layout.wizard.LayoutWizard;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;

import java.io.FileReader;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class DisplayDriverFx implements DisplayDriver
{
	public DisplayDriverFx(MatrixTreeView treeView, Context context, MatrixContextMenu rowContextMenu, MatrixParametersContextMenu parametersContextMenu)
	{
		this.treeView = treeView;
		this.context = context;
		this.rowContextMenu = rowContextMenu;
		this.parametersContextMenu = parametersContextMenu;
	}

	private void selectCurrentRow(MatrixTreeRow row)
	{
		if (!row.isSelected())
		{
			this.treeView.getSelectionModel().clearSelection();
			this.treeView.getFocusModel().focus(this.treeView.getRow(row.getTreeItem()));
			this.treeView.getSelectionModel().select(row.getTreeItem());
		}
	}

	@Override
	public Object createLayout(MatrixItem item, int lines)
	{
		GridPane pane = new GridPane();
		if (item.getParent() == null)
		{
			TreeItem<MatrixItem> root = new TreeItem<>(item);
			this.treeView.setRoot(root);
		}
		else
		{
			MatrixItem parent = item.getParent();
			TreeItem<MatrixItem> treeItem = this.treeView.find(parent);
			int insertIndex = 0;
			for (int i = 0; i < parent.count(); i++)
			{
				if (parent.get(i) == item)
				{
					insertIndex = i;
					break;
				}
			}
			TreeItem<MatrixItem> newTreeItem = new TreeItem<>(item);
			if (lines == -1)
			{
				treeItem.getChildren().add(newTreeItem);
			}
			else
			{
				treeItem.getChildren().add(insertIndex, newTreeItem);
			}
		}
		return pane;
	}

	@Override
	public void showTitle(MatrixItem item, Object layout, int row, int column, String name, Settings settings)
	{
		GridPane pane = (GridPane) layout;

		final Label label = new Label(name);
		label.getStyleClass().add(CssVariables.BOLD_LABEL);
		label.getStyleClass().add(CssVariables.OBLIQUE_LABEL);
		label.setOnMouseClicked(mouseEvent -> pane.getChildren().stream().filter(c ->
		{
			Integer rowIndex = GridPane.getRowIndex(c);
			Integer columnIndex = GridPane.getColumnIndex(c);
			return rowIndex != null && columnIndex != null && rowIndex == 0 && columnIndex == 0;
		}).findFirst().ifPresent(Node::requestFocus));
		label.setMinWidth(name.length() * 9);
		pane.add(label, column, row);
		GridPane.setMargin(label, INSETS);
		if (item instanceof ActionItem)
		{
			ActionItem actionItem = (ActionItem) item;
			String actionName = actionItem.getActionName();
			Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_COLORS, actionName);
			if (value != null)
			{
				label.setStyle("-fx-text-fill : " + value.getValue());
			}
			else
			{
				updateStyle(actionItem.group().name(), settings, label);
			}
		}
		else
		{
			updateStyle(item.getClass().getSimpleName(), settings, label);
		}
		Common.setFocused(label);
	}

	@Override
	public void showLabel(MatrixItem item, Object layout, int row, int column, String name, int fontSize)
	{
		GridPane pane = (GridPane) layout;
		Label label = new Label(name);
		if (fontSize != -1)
		{
			label.setFont(javafx.scene.text.Font.font(fontSize));
		}
		if (Tokens.contains(name))
		{
			label.getStyleClass().add(CssVariables.BOLD_LABEL);
		}
		//		label.setPrefWidth(getPrefWidth(name));
		Common.sizeLabel(label);
		GridPane.setMargin(label, new Insets(0, 0, 0, 5));
		Platform.runLater(() -> label.setTooltip(new Tooltip(name)));
		pane.add(label, column, row);
		GridPane.setMargin(label, INSETS);
	}

	@Override
	public void showCheckBox(MatrixItem item, Object layout, int row, int column, String name, Setter<Boolean> set, Getter<Boolean> get)
	{
		GridPane pane = (GridPane) layout;
		CheckBox checkBox = new CheckBox(name);
		checkBox.setMinWidth(name.length() * 8 + 20);
		checkBox.setSelected(get.get());
		checkBox.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			}
		});
		checkBox.setOnAction(e ->
		{
			Boolean lastValue = get.get();
			Boolean value = checkBox.isSelected();
			if (lastValue == value)
			{
				return;
			}

			Command undo = () ->
			{
				set.set(lastValue);
				checkBox.setSelected(lastValue);
			};
			Command redo = () ->
			{
				set.set(value);
				checkBox.setSelected(value);
			};
			item.getMatrix().addCommand(undo, redo);
		});
		addToLayout(checkBox, column, row, pane);
		//		pane.add(checkBox, column, row);
		GridPane.setMargin(checkBox, INSETS);
	}

	@Override
	public void showComboBox(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, Function<Void, List<String>> handler)
	{
		GridPane pane = (GridPane) layout;
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.setValue(get.get());
		comboBox.setOnAction(e ->
		{
			String lastValue = get.get();
			String value = comboBox.getValue();
			if (Str.areEqual(lastValue, value))
			{
				return;
			}

			Command undo = () ->
			{
				set.set(lastValue);
				comboBox.setValue(lastValue);
			};
			Command redo = () ->
			{
				set.set(value);
				comboBox.setValue(value);
			};
			item.getMatrix().addCommand(undo, redo);
		});
		if (handler != null)
		{
			comboBox.showingProperty().addListener((observable, oldValue, newValue) ->
			{
				if (!oldValue && newValue)
				{
					List<String> list = handler.apply(null);
					comboBox.setItems(FXCollections.observableArrayList(list));
				}
			});

		}
		pane.add(comboBox, column, row);
		GridPane.setMargin(comboBox, INSETS);
	}

	@Override
	public void showTextBox(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> get, FormulaGenerator generator)
	{
		GridPane pane = (GridPane) layout;

		TextField textBox = new TextField();
		textBox.setContextMenu(this.rowContextMenu);
		textBox.setStyle(Common.FONT_SIZE);
		textBox.setText(get.get());
		Common.sizeTextField(textBox);
		textBox.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			}
			if (!newValue && oldValue)
			{
				String lastValue = get.get();
				String value = textBox.getText();
				if (Str.areEqual(lastValue, value))
				{
					return;
				}

				Command undo = () ->
				{
					set.set(lastValue);
					textBox.setText(lastValue);
					stretchIfCan(textBox);
				};
				Command redo = () ->
				{
					set.set(value);
					textBox.setText(value);
					stretchIfCan(textBox);
				};
				item.getMatrix().addCommand(undo, redo);
			}
		});

		if (generator != null)
		{
			textBox.setOnDragDetected(new DragDetector(generator)::onDragDetected);
		}

		GridPane gridPane = new GridPane();
		gridPane.add(textBox, 0, 0);
		pane.add(gridPane, column, row);
		GridPane.setMargin(textBox, INSETS);
		//		Common.setFocused(textBox);
	}

	@Override
	public void showExpressionField(MatrixItem item, Object layout, int row, int column, String name, Setter<String> set, Getter<String> get, Function<String, String> firstHandler, Function<String, String> secondHandler, Character first, Character second)
	{
		GridPane pane = (GridPane) layout;

		ExpressionField field = new ExpressionField(this.context.getEvaluator(), get.get());
		field.setContextMenu(this.rowContextMenu);
		field.setFirstActionListener(firstHandler);
		field.setSecondActionListener(secondHandler);
		field.setChangingValueListener((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			}
			if (!newValue && oldValue)
			{
				String lastValue = get.get();
				String value = field.getText();
				if (Str.areEqual(lastValue, value))
				{
					return;
				}

				Command undo = () ->
				{
					set.set(lastValue);
					field.setText(lastValue);
				};
				Command redo = () ->
				{
					set.set(value);
					field.setText(value);
				};
				item.getMatrix().addCommand(undo, redo);
			}
		});
		if (first != null)
		{
			field.setNameFirst("" + first);
		}
		if (second != null)
		{
			field.setNameSecond("" + second);
		}
		if (secondHandler == null)
		{
			field.setHelperForExpressionField(name, item.getMatrix());
		}
		GridPane temp = new GridPane();
		temp.add(field, 0, 0);
		pane.add(temp, column, row);
		GridPane.setMargin(field, INSETS);
	}

	@Override
	public void showTextArea(MatrixItem item, Object layout, int row, int column, Text text, Consumer<List<String>> consumer)
	{
		GridPane pane = (GridPane) layout;
		TextArea textArea = new TextArea();
		textArea.setPrefWidth(Double.MAX_VALUE);
		DragResizer.makeResizable(textArea, textArea::setPrefHeight);
		for (String s : text)
		{
			textArea.appendText(s + "\n");
		}
		textArea.textProperty().addListener((observable, oldValue, newValue) ->
		{
			String[] split = newValue.split("\n");
			consumer.accept(Arrays.asList(split));
		});
		pane.add(textArea, column, row, Integer.MAX_VALUE, 1);

	}

	@Override
	public void showAutoCompleteBox(MatrixItem item, Object layout, int row, int column, List<String> words, Consumer<String> supplier)
	{
		GridPane pane = (GridPane) layout;
		TextField field = new TextField("");
		AutoCompletionTextFieldBinding<String> binding = new AutoCompletionTextFieldBinding<>(field, SuggestionProvider.create(words));
		binding.setOnAutoCompleted(e -> accept(words, supplier, field));
		List<String> tempList = words.stream().map(String::toLowerCase).collect(Collectors.toList());
		field.textProperty().addListener((observable1, oldValue1, newValue1) ->
		{
			boolean present = tempList.stream().filter(s -> s.contains(newValue1.toLowerCase())).findFirst().isPresent();
			if (present)
			{
				field.setStyle("-fx-text-fill : green");
			}
			else
			{
				field.setStyle("-fx-text-fill : red");
			}
		});
		field.setOnAction(e -> accept(words, supplier, field));
		field.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue && oldValue)
			{
				accept(words, supplier, field);
			}
		});
		pane.add(field, column, row);
		GridPane.setMargin(field, INSETS);
		Common.setFocused(field);
	}

	@Override
	public void showComment(MatrixItem item, Object layout, int row, int column, List<CommentString> comments)
	{
		GridPane pane = (GridPane) layout;

		CommentsLabel label = new CommentsLabel();
		label.setPrefWidth(Double.MAX_VALUE);
		label.setContextMenu(this.rowContextMenu);
		label.getStyleClass().addAll(CssVariables.UNFOCUSED_TEXT_AREA);
		label.setText(fromList(comments));
		if (label.getText().isEmpty())
		{
			Common.sizeHeightComments(label, 0, 1, 2);
		}
		label.prefWidthProperty().bind(CustomTabPane.getInstance().getScene().getWindow().widthProperty().subtract(20 + 15));
		label.setPrefHeight(Common.setHeightComments(label.getText()));
		label.setListener(() ->
		{
			String lastValue = fromList(comments);
			String value = label.getText();
			if (Str.areEqual(lastValue, value))
			{
				return;
			}

			Command undo = () ->
			{
				comments.clear();
				comments.addAll(fromStr(lastValue));
				label.setText(lastValue);
				label.setPrefHeight(Common.setHeightComments(lastValue));
			};
			Command redo = () ->
			{
				comments.clear();
				comments.addAll(fromStr(value));
				label.setText(value);
				label.setPrefHeight(Common.setHeightComments(value));
			};
			item.getMatrix().addCommand(undo, redo);
		});

		pane.add(label, column, row, Integer.MAX_VALUE, 1);
	}

	@Override
	public void showButton(MatrixItem item, Object layout, int row, int column, String name, Function<MatrixItem, Void> action)
	{
		GridPane pane = (GridPane) layout;
		Button button = new Button(name);
		button.setOnAction(e -> action.apply(item));
		pane.add(button, column, row);
		GridPane.setMargin(button, INSETS);
	}

	@Override
	public void showToggleButton(MatrixItem item, Object layout, int row, int column, String name, Function<Boolean, Void> action, Function<Boolean, String> changeName, boolean initialValue)
	{
		GridPane pane = (GridPane) layout;
		ToggleButton toggleButton = new ToggleButton(name);
		toggleButton.setSelected(initialValue);
		toggleButton.setOnAction(e ->
		{
			selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			boolean isNotSelected = !toggleButton.isSelected();
			action.apply(isNotSelected);
			toggleButton.setText(changeName.apply(!isNotSelected));
		});
		if (!initialValue)
		{
			action.apply(true);
		}
		pane.add(toggleButton, column, row);
		GridPane.setMargin(toggleButton, INSETS);
	}

	@Override
	public void showParameters(MatrixItem item, Object layout, int row, int column, Parameters parameters, FormulaGenerator generator, boolean oneLine)
	{
		GridPane pane = (GridPane) layout;

		ParametersPane paramsPane = new ParametersPane(item, this.context, oneLine, parameters, generator, this.rowContextMenu, this.parametersContextMenu, () -> selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent())));
		GridPane.setMargin(paramsPane, new Insets(column, 10, column, 10));
		pane.add(paramsPane, column, row, Integer.MAX_VALUE, 2);
	}

	@Override
	public void showGrid(MatrixItem item, Object layout, int row, int column, Table table)
	{
		GridPane pane = (GridPane) layout;
		DataProvider<String> provider = new TableDataProvider(table);
		SpreadsheetView view = new SpreadsheetView(provider);
		view.setContextMenu(this.rowContextMenu);
		BorderPane borderPane = new BorderPane();
		borderPane.setCenter(view);
		DragResizer.makeResizable(borderPane, view::setPrefHeight);
		view.setPrefHeight(30 * (Math.min(provider.getRowHeaders().size(), 4) + 1));
		BorderPane newPane = new BorderPane();
		newPane.setCenter(borderPane);
		BorderPane.setMargin(newPane, new Insets(0, 0, 10, 0));
		pane.add(newPane, column, row, 6, 2);
	}

	@Override
	public void hide(MatrixItem item, Object layout, int row, boolean hide)
	{
		GridPane pane = (GridPane) layout;
		pane.getChildren().stream().filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) == row).forEach(c ->
		{
			((Region) c).setPrefHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
			((Region) c).setMaxHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
			((Region) c).setMinHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
			c.setVisible(!hide);
		});
	}

	@Override
	public void setupCall(MatrixItem item, String reference, Parameters parameters)
	{
		((MatrixFx) item.getMatrix()).setupCall(item, reference, parameters);
	}

	@Override
	public void setCurrentItem(MatrixItem item)
	{
		if (item == null)
		{
			return;
		}

		Matrix matrix = item.getMatrix();
		CustomTab tab = Common.checkDocument(matrix);
		Matrix matrixFx = null;
		if (tab != null)
		{
			matrixFx = (MatrixFx) tab.getDocument();
			tab.getTabPane().getSelectionModel().select(tab);
		}
		else
		{
			try
			{
				matrixFx = context.getFactory().createMatrix(matrix.getName()); // TODO weird
				matrixFx.load(new FileReader(matrix.getName()));
				matrixFx.display();
			}
			catch (Exception e)
			{
				DialogsHelper.showError("Couldn't open the matrix " + matrixFx);
			}
		}
		if (matrixFx != null)
		{
			((MatrixFx) matrixFx).setCurrent(item);
		}
	}

	@Override
	public void deleteItem(MatrixItem item)
	{
		if (item == null)
		{
			return;
		}
		TreeItem<MatrixItem> treeItem = this.treeView.find(item);
		TreeItem<MatrixItem> parent = treeItem.getParent();
		if (parent != null)
		{
			parent.getChildren().remove(treeItem);
		}
	}

	@Override
	public void layoutWizard(MatrixItem item, Table table, Context context)
	{
		AppConnection defaultApplicationConnection = item.getMatrix().getDefaultApplicationConnection();
		if (defaultApplicationConnection == null)
		{
			DialogsHelper.showInfo("You need to start application");
			return;
		}
		LayoutWizard wizard = new LayoutWizard(table, defaultApplicationConnection, context.getEvaluator());
		wizard.show();
	}

	private void updateStyle(String key, Settings settings, Label label)
	{
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_COLORS, key);
		Optional.ofNullable(value).ifPresent(v -> label.setStyle("-fx-text-fill : " + v.getValue()));
	}

	private void addToLayout(Node node, int column, int row, GridPane layout)
	{
		Optional<Node> first = layout.getChildren().stream().filter(Objects::nonNull).filter(n ->
		{
			Integer columnIndex = GridPane.getColumnIndex(n);
			Integer rowIndex = GridPane.getRowIndex(n);
			return columnIndex != null && rowIndex != null && columnIndex == column && rowIndex == row;
		}).findFirst();

		if (first.isPresent())
		{
			Node get = first.get();
			if (get instanceof Pane)
			{
				Pane pane = (Pane) get;
				pane.getChildren().add(node);
			}
			else
			{
				layout.getChildren().remove(get);

				HBox hBox = new HBox();
				hBox.setAlignment(Pos.CENTER_LEFT);
				hBox.getChildren().addAll(get, node);
				hBox.setSpacing(5);
				GridPane.setMargin(hBox, INSETS);

				layout.add(hBox, column, row);
			}
		}
		else
		{
			layout.add(node, column, row);
		}
	}

	private void stretchIfCan(TextField tf)
	{
		int size = getPrefWidth(tf.getText());
		if (tf.getScene() != null)
		{
			double v = tf.getScene().getWindow().getWidth() / 3;
			if (size > v)
			{
				tf.setPrefWidth(v);
				return;
			}
		}
		tf.setPrefWidth(size);
	}

	private int getPrefWidth(String text)
	{
		return text != null ? (text.length() * 8 + 20) : 60;
	}

	private List<CommentString> fromStr(String str)
	{
		// because TextArea from javafx split line via \n, not via System.lineSeparator()
		return Arrays.asList(str.split("\n")).stream().map(CommentString::new).collect(Collectors.toList());
	}

	private String fromList(List<CommentString> list)
	{
		Optional<CommentString> opt = list.stream().reduce((cs1, cs2) -> new CommentString(cs1.toString() + LINE_SEPARATOR + cs2.toString()));
		return opt.isPresent() ? opt.get().toString() : "";
	}

	private void accept(List<String> words, Consumer<String> supplier, TextField field)
	{
		Optional<String> first = words.stream().filter(field.getText()::equalsIgnoreCase).findFirst();
		if (first.isPresent())
		{
			supplier.accept(first.get());
		}
		else
		{
			supplier.accept(field.getText());
		}
	}


	private static final Insets INSETS = new Insets(0, 0, 0, 5);
	private static final String LINE_SEPARATOR = System.getProperty("line.separator");
	private MatrixTreeView treeView;
	private Context context;
	private MatrixContextMenu rowContextMenu;
	private MatrixParametersContextMenu parametersContextMenu;
}
