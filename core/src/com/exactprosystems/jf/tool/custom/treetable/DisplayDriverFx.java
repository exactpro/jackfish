////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.api.client.IMessageDictionary;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.highlighter.Highlighter;
import com.exactprosystems.jf.common.undoredo.Command;
import com.exactprosystems.jf.documents.Document;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.*;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.CommentString;
import com.exactprosystems.jf.documents.matrix.parser.items.End;
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
import com.exactprosystems.jf.tool.custom.number.NumberSpinner;
import com.exactprosystems.jf.tool.custom.number.NumberTextField;
import com.exactprosystems.jf.tool.custom.tab.CustomTab;
import com.exactprosystems.jf.tool.custom.tab.CustomTabPane;
import com.exactprosystems.jf.tool.documents.AbstractDocumentController;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixFxController;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import org.fxmisc.richtext.StyleClassedTextArea;
import org.reactfx.Subscription;

import java.io.Reader;
import java.util.*;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class DisplayDriverFx implements DisplayDriver
{
	private static final String GRID_PARENT           = "gridParent";
	private static final String GRID_PARENT_EXP_FIELD = "gridParentExpField";

	private              Map<GridPane, Subscription> mapSubscribers = new HashMap<>();
	private static final Insets                      INSETS         = new Insets(0, 0, 0, 5);
	private MatrixTreeView              treeView;
	private Context                     context;
	private MatrixContextMenu           rowContextMenu;
	private MatrixParametersContextMenu parametersContextMenu;

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
		//this true only for RootItem
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
			//this true only for End elements
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
		label.getStyleClass().addAll(CssVariables.BOLD_LABEL, CssVariables.OBLIQUE_LABEL);
		label.setOnMouseClicked(mouseEvent -> pane.getChildren()
				.stream()
				.filter(c ->
				{
					Integer rowIndex = GridPane.getRowIndex(c);
					Integer columnIndex = GridPane.getColumnIndex(c);
					return rowIndex != null && columnIndex != null && rowIndex == 0 && columnIndex == 0;
				})
				.findFirst()
				.ifPresent(Common::setFocusedFast));
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
		Common.setFocusedFast(label);
	}

	@Override
	public void showLabel(MatrixItem item, Object layout, int row, int column, String name)
	{
		GridPane pane = (GridPane) layout;
		Label label = new Label(name);
		if (item instanceof End)
		{
			label.setFont(javafx.scene.text.Font.font(10));
			label.getStyleClass().add(CssVariables.BOLD_LABEL);
		}
		else
		{
			Common.sizeLabel(label);
		}
		if (Tokens.contains(name))
		{
			label.getStyleClass().add(CssVariables.BOLD_LABEL);
		}
		Common.runLater(() -> label.setTooltip(new Tooltip(name)));
		pane.add(label, column, row);
		GridPane.setMargin(label, INSETS);
	}

	@Override
	public void showCheckBox(MatrixItem item, Object layout, int row, int column, String name, Setter<Boolean> setter, Getter<Boolean> getter)
	{
		GridPane pane = (GridPane) layout;
		CheckBox checkBox = new CheckBox(name);
		checkBox.setMinWidth(name.length() * 8 + 20);
		checkBox.setSelected(getter.get());
		checkBox.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			}
		});
		checkBox.setOnAction(e ->
		{
			Boolean lastValue = getter.get();
			Boolean value = checkBox.isSelected();
			if (lastValue == value)
			{
				return;
			}

			Command undo = () ->
			{
				setter.set(lastValue);
				checkBox.setSelected(lastValue);
			};
			Command redo = () ->
			{
				setter.set(value);
				checkBox.setSelected(value);
			};
			item.getMatrix().addCommand(undo, redo);
		});
		addToLayout(checkBox, column, row, pane);
		GridPane.setMargin(checkBox, INSETS);
	}

	@Override
	public void showComboBox(MatrixItem item, Object layout, int row, int column, Setter<String> setter, Getter<String> getter, Supplier<List<String>> handler, Function<String, Boolean> needUpdate)
	{
		GridPane pane = (GridPane) layout;
		ComboBox<String> comboBox = new ComboBox<>();
		comboBox.setValue(getter.get());
		comboBox.setOnAction(event -> {
			if (comboBox.getValue() == null)
			{
				return;
			}
			String lastValue = getter.get();
			String newValue = comboBox.getValue();
			if (Str.areEqual(lastValue, newValue))
			{
				return;
			}

			//TODO
			if (!needUpdate.apply(newValue))
			{
				comboBox.getSelectionModel().select(lastValue);
				return;
			}

			Command undo = () ->
			{
				setter.set(lastValue);
				comboBox.getSelectionModel().select(lastValue);
			};
			Command redo = () ->
			{
				setter.set(newValue);
				comboBox.getSelectionModel().select(newValue);
			};
			item.getMatrix().addCommand(undo, redo);
		});
		if (handler != null)
		{
			comboBox.showingProperty().addListener((observable, oldValue, newValue) ->
			{
				if (!oldValue && newValue)
				{
					List<String> list = handler.get();
					comboBox.setItems(FXCollections.observableArrayList(list));
				}
			});

		}
		pane.add(comboBox, column, row);
		GridPane.setMargin(comboBox, INSETS);
	}

	@Override
	public void showTextBox(MatrixItem item, Object layout, int row, int column, Setter<String> set, Getter<String> getter, FormulaGenerator generator)
	{
		GridPane pane = (GridPane) layout;

		TextField textBox = new TextField();
		textBox.setContextMenu(this.rowContextMenu);
		textBox.setStyle(Common.FONT_SIZE);
		textBox.setText(getter.get());
		Common.sizeTextField(textBox);
		textBox.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			}
			if (!newValue && oldValue)
			{
				String lastValue = getter.get();
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
		textBox.textProperty().addListener((observable, oldValue, newValue) -> textBox.setPrefWidth(Common.computeTextWidth(textBox.getFont(), textBox.getText(), 0.0D) + 40));

		if (generator != null)
		{
			textBox.setOnDragDetected(new DragDetector(generator)::onDragDetected);
		}

		GridPane gridPane = new GridPane();
		gridPane.add(textBox, 0, 0);
		pane.add(gridPane, column, row);
		GridPane.setMargin(textBox, INSETS);
	}

	@Override
	public void showExpressionField(MatrixItem item, Object layout, int row, int column, String name, Setter<String> set, Getter<String> get, Function<String, String> firstHandler, Function<String, String> secondHandler, Character first, Character second)
	{
		GridPane pane = (GridPane) layout;

		ExpressionField field = new ExpressionField(this.context.getEvaluator(), get.get());
		field.setStretchable(true);
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
		temp.getStyleClass().add(GRID_PARENT_EXP_FIELD);
		temp.add(field, 0, 0);
		pane.add(temp, column, row);
		if (item instanceof ActionItem && Str.areEqual(name, Tokens.Assert.get()))
		{
			GridPane.setColumnSpan(temp, 2);
		}
		GridPane.setMargin(field, INSETS);
	}

	@Override
	public void showTextArea(MatrixItem item, Object layout, int row, int column, Text text, Consumer<List<String>> consumer, Highlighter highlighter)
	{
		GridPane pane = (GridPane) layout;
		StyleClassedTextArea textArea = new StyleClassedTextArea();
		DragResizer.makeResizable(textArea, textArea::setPrefHeight);
		textArea.setPrefWidth(Double.MAX_VALUE);
		textArea.getUndoManager().close();
		textArea.appendText(text.stream().collect(Collectors.joining("\n")));
		Optional.ofNullable(this.mapSubscribers.get(pane)).ifPresent(Subscription::unsubscribe);
		textArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(textArea.getText())));
		this.mapSubscribers.putIfAbsent(pane, textArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> textArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(textArea.getText()))))
		);

		textArea.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue && oldValue)
			{
				consumer.accept(Arrays.asList(textArea.getText().split("\n")));
			}
		});
		pane.add(textArea, column, row, Integer.MAX_VALUE, 1);
	}

	@Override
	public void displayHighlight(Object layout, Highlighter highlighter)
	{
		GridPane gridPane = (GridPane) layout;

		Node node = gridPane.getChildren().stream()
				.filter(child -> child instanceof StyleClassedTextArea)
				.findFirst()
				.orElseThrow(() -> new RuntimeException("Never"));

		StyleClassedTextArea styledTextArea = (StyleClassedTextArea) node;
		Optional.ofNullable(this.mapSubscribers.get(gridPane)).ifPresent(Subscription::unsubscribe);
		styledTextArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(styledTextArea.getText())));
		this.mapSubscribers.putIfAbsent(gridPane, styledTextArea.richChanges()
				.filter(ch -> !ch.getInserted().equals(ch.getRemoved()))
				.subscribe(change -> styledTextArea.setStyleSpans(0, Common.convertFromList(highlighter.getStyles(styledTextArea.getText()))))
		);
	}

	@Override
	public void updateTextArea(MatrixItem item, Object layout, Text text)
	{
		GridPane pane = (GridPane) layout;
		Node lookup = pane.lookup(".styled-text-area");
		if (lookup instanceof StyleClassedTextArea)
		{
			StyleClassedTextArea ta = (StyleClassedTextArea) lookup;
			ta.clear();
			ta.appendText(text.stream().collect(Collectors.joining("\n")));
		}
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
			List<String> match = SuggestionProvider.isMatch(tempList, newValue1);
			if (!match.isEmpty())
			{
				//TODO remake this via CssVariables and styleClasses
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
		Common.setFocusedFast(field);
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
	public void showButton(MatrixItem item, Object layout, int row, int column, String name, Consumer<MatrixItem> action)
	{
		GridPane pane = (GridPane) layout;
		Button button = new Button(name);
		button.setOnAction(e -> action.accept(item));
		pane.add(button, column, row);
		GridPane.setMargin(button, INSETS);
	}

	@Override
	public void showSpinner(MatrixItem item, Object layout, int row, int column, double prefWidth, Setter<Integer> set, Getter<Integer> get, int minValue, int maxValue)
	{
		GridPane pane = ((GridPane) layout);

		Integer initialValue = get.get();
		NumberTextField numberField = new NumberTextField(initialValue, minValue, maxValue);
		numberField.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			if (!newValue && oldValue)
			{
				if (Str.IsNullOrEmpty(numberField.getText()))
				{
					DialogsHelper.showInfo("Field can't be empty. Initial size was setted");
					numberField.setText("" + initialValue);
				}
				set.set(numberField.getValue());
			}
		});
		NumberSpinner spinner = new NumberSpinner(numberField);
		spinner.setPrefWidth(prefWidth);

		pane.add(spinner, column, row);
	}

	@Override
	public void extendsTable(Object layout, int prefCols, int prefRows, BooleanSupplier supplier)
	{
		if (!supplier.getAsBoolean())
		{
			return;
		}
		((GridPane) layout).getChildren()
				.stream()
				.filter(node -> node.getStyleClass().contains(GRID_PARENT))
				.map(node -> (BorderPane) node)
				.findFirst()
				.ifPresent(bp ->
				{
					SpreadsheetView spreadsheetView = (SpreadsheetView) bp.getCenter();
					spreadsheetView.getProvider().extendsTable(prefCols, prefRows);
				});
	}

	@Override
	public void showToggleButton(MatrixItem item, Object layout, int row, int column, Consumer<Boolean> action, Function<Boolean, String> changeName, boolean initialValue)
	{
		GridPane pane = (GridPane) layout;
		ToggleButton toggleButton = new ToggleButton(changeName.apply(initialValue));
		toggleButton.setSelected(initialValue);
		toggleButton.setOnAction(e ->
		{
			selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent()));
			boolean isNotSelected = !toggleButton.isSelected();
			action.accept(isNotSelected);
			toggleButton.setText(changeName.apply(!isNotSelected));
		});
		if (!initialValue)
		{
			action.accept(true);
		}
		pane.add(toggleButton, column, row);
		GridPane.setMargin(toggleButton, INSETS);
	}

	@Override
	public void showParameters(MatrixItem item, Object layout, int row, int column, Parameters parameters, FormulaGenerator generator, boolean oneLine)
	{
		GridPane pane = (GridPane) layout;

		ParametersPane paramsPane = new ParametersPane(item, this.context, oneLine, parameters, generator, this.rowContextMenu, this.parametersContextMenu, () -> selectCurrentRow(((MatrixTreeRow) pane.getParent().getParent())));
		GridPane.setMargin(paramsPane, new Insets(column, 8, column, 8));
		pane.add(paramsPane, column, row, Integer.MAX_VALUE, 2);
	}

	@Override
	public void showGrid(MatrixItem item, Object layout, int row, int column, Table table)
	{
		GridPane pane = (GridPane) layout;
		DataProvider<String> provider = new TableDataProvider(table, item.getMatrix()::addCommand);
		SpreadsheetView view = new SpreadsheetView(provider);
		provider.displayFunction(view::display);
		view.setPrefHeight(25 * (Math.min(provider.getRowHeaders().size(), 50) + 1));

		BorderPane borderPane = new BorderPane();
		borderPane.getStyleClass().add(GRID_PARENT);
		borderPane.setCenter(view);
		DragResizer.makeResizable(borderPane, view::setPrefHeight);
		BorderPane.setMargin(borderPane, new Insets(0, 0, 10, 0));

		pane.add(borderPane, column, row, 10, 2);
	}

	@Override
	public void showTree(MatrixItem item, Object layout, int row, int column, MapMessage message, IMessageDictionary dictionary, Context context)
	{
		GridPane pane = (GridPane) layout;
		RawMessageTreeView messageView = new RawMessageTreeView(context.getEvaluator());
		messageView.displayTree(message, dictionary);
		DragResizer.makeResizable(messageView, messageView::setPrefHeight);
		pane.add(messageView, column, row, 10, 2);
	}

	@Override
	public void updateTree(MatrixItem item, Object layout, MapMessage message, IMessageDictionary dictionary)
	{
		((GridPane) layout).getChildren().stream()
				.filter(node -> node instanceof RawMessageTreeView)
				.findFirst()
				.map(node -> (RawMessageTreeView)node)
				.ifPresent(rawMessageTreeView -> rawMessageTreeView.displayTree(message, dictionary));
	}

	@Override
	public void hide(MatrixItem item, Object layout, int row, boolean hide)
	{
		GridPane pane = (GridPane) layout;
		pane.getChildren()
				.stream()
				.filter(child -> GridPane.getRowIndex(child) != null && GridPane.getRowIndex(child) == row)
				.forEach(c ->
				{
					((Region) c).setPrefHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
					((Region) c).setMaxHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
					((Region) c).setMinHeight(hide ? 0 : Control.USE_COMPUTED_SIZE);
					c.setVisible(!hide);
					if (c.isVisible() && c.getStyleClass().contains(GRID_PARENT_EXP_FIELD))
					{
						((GridPane) c).getChildren()
								.stream()
								.filter(node -> node instanceof ExpressionField)
								.findFirst()
								.ifPresent(Common::setFocusedFast);
					}
				});
	}

	@Override
	public void setupCall(MatrixItem item, String reference, Parameters parameters)
	{
		((MatrixFx) item.getMatrix()).setupCall(item, reference, parameters);
	}

	@Override
	public void setCurrentItem(MatrixItem item, Matrix matrix, boolean needExpand)
	{
		if (item == null || matrix == null)
		{
			return;
		}

		CustomTab tab = Common.checkDocument(matrix);
		if (tab == null)
		{
			Common.tryCatch(() -> {
				try(Reader reader = CommonHelper.readerFromFileName(matrix.getNameProperty().get()))
				{
					Matrix matrixFx = (Matrix) context.getFactory().createDocument(DocumentKind.MATRIX, matrix.getNameProperty().get());
					matrixFx.load(reader);
					context.getFactory().showDocument(matrixFx);
				}
			}, "Couldn't open the matrix " + matrix.getNameProperty().get());
			tab = Common.checkDocument(matrix);
		}
		if (tab != null)
		{
			tab.getTabPane().getSelectionModel().select(tab);
			AbstractDocumentController<? extends Document> controller = tab.getController();
			if (controller instanceof MatrixFxController)
			{
				((MatrixFxController) controller).setCurrent(item, needExpand);
			}
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
		if (treeItem != null)
		{
			TreeItem<MatrixItem> parent = treeItem.getParent();
			if (parent != null)
			{
				parent.getChildren().remove(treeItem);
			}
		}
	}

	//region private methods
	private void updateStyle(String key, Settings settings, Label label)
	{
		Settings.SettingsValue value = settings.getValue(Settings.GLOBAL_NS, Settings.MATRIX_COLORS, key);
		Optional.ofNullable(value).ifPresent(v -> label.setStyle("-fx-text-fill : " + v.getValue()));
	}

	private void addToLayout(Node node, int column, int row, GridPane layout)
	{
		Optional<Node> first = layout.getChildren().stream()
				.filter(Objects::nonNull)
				.filter(n ->
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
		double size = getPrefWidth(tf.getText(), tf.getFont());
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

	private double getPrefWidth(String text, Font font)
	{
		return Common.computeTextWidth(font, text, 0.0D) + 40;
	}

	private List<CommentString> fromStr(String str)
	{
		return Arrays.stream(str.split("\n"))
				.map(CommentString::new)
				.collect(Collectors.toList());
	}

	private String fromList(List<CommentString> list)
	{
		return list.stream()
				.map(CommentString::toString)
				.collect(Collectors.joining("\n"));
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
	//endregion
}
