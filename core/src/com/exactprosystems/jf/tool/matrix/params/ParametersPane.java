////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.DialogFill;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.FormulaGenerator;
import com.exactprosystems.jf.common.parser.Parameter;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Xml;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.DragDetector;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.layout.LayoutExpressionBuilder;
import com.exactprosystems.jf.tool.custom.scroll.CustomScrollPane;
import com.exactprosystems.jf.tool.custom.treetable.MatrixContextMenu;
import com.exactprosystems.jf.tool.custom.treetable.MatrixParametersContextMenu;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.DialogsHelper.OpenSaveMode;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.io.File;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;

public class ParametersPane extends CustomScrollPane
{
	private GridPane	mainGridPane;
	private Context		context;
	private Parameters	parameters;
	private MatrixItem	matrixItem;
	private boolean 	oneLine;
	private FormulaGenerator generator;
	private EventHandler<ContextMenuEvent> contextMenuHandler;
	private Common.Function fnc;
	private static ContextMenu empty = new ContextMenu();

	public ParametersPane(MatrixItem matrixItem, Context context, boolean oneLine, Parameters parameters, FormulaGenerator generator,
						  MatrixContextMenu rowContextMenu, MatrixParametersContextMenu parametersContextMenu, Common.Function fnc)
	{
		super(oneLine ? 30 : 65);
		this.fnc = fnc;
		this.mainGridPane = new GridPane();
		this.setContent(this.mainGridPane);
		this.matrixItem = matrixItem;
		this.context = context;
		this.oneLine = oneLine;
		this.parameters = parameters;
		this.generator = generator;
		
		this.contextMenuHandler = parametersContextMenu.createContextMenuHandler();

		super.setContextMenu(rowContextMenu);
		refreshParameters();
	}

	public void refreshParameters()
	{
		ObservableList<Node> children = FXCollections.observableArrayList(this.mainGridPane.getChildren());

		this.mainGridPane.getChildren().clear();
		for (int i = 0; i < this.parameters.size(); i++)
		{
			Parameter par = this.parameters.getByIndex(i);
			Pane exist = findPane(children, par);
			if (exist == null)
			{
				exist = parameterBox(par, i, this.contextMenuHandler);
			}
			updatePane(exist, par, i);
			this.mainGridPane.add(exist, i + 1, 0, 1, this.oneLine ? 1 : 2);
		}
		this.mainGridPane.add(emptyBox(FXCollections.observableArrayList(this.mainGridPane.getChildren()), this.contextMenuHandler), 0, 0, 1, 2);
	}

	private void updatePane(Pane pane, Parameter parameter, int index)
	{
		pane.getChildren().stream()
				.filter(node -> node.getClass() == TextField.class)
				.findFirst()
				.ifPresent(field -> {
					field.focusedProperty().addListener(createKeyChangeListener(parameter, ((TextField) field), index));
					((TextField) field).setText(parameter.getName());
				});

		pane.getChildren().stream()
				.filter(node -> node.getClass() == ExpressionField.class)
				.findFirst()
				.ifPresent(field -> {
					((ExpressionField) field).setChangingValueListener(createExpressionChangeListener(parameter, ((ExpressionField) field), index));
					((ExpressionField) field).setText(parameter.getExpression());
					((ExpressionField) field).stretchIfCan(parameter.getExpression());
				});
	}

	private Pane findPane(ObservableList<Node> children, Parameter par)
	{
		Optional<GridPane> opt = children.stream()
				.filter(node -> 
						{
							if (node instanceof GridPane)
							{
								GridPane gridPane = (GridPane)node;
								Object obj = gridPane.getUserData();
								if (obj instanceof Parameter)
								{
									return (Parameter)obj == par;
								}
							}
							return false;
						})
				.map(node -> (GridPane)node)
				.findFirst();
		
		return opt.isPresent() ? opt.get() : null;
	}

	private Pane emptyBox(ObservableList<Node> children, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		GridPane pane = new EmptyParameterGridPane();
		pane.getStyleClass().add(CssVariables.EMPTY_GRID);

		Label emptyLabel = new Label();
		emptyLabel.getStyleClass().add(CssVariables.INVISIBLE_FIELD);
		emptyLabel.setPrefWidth(15);
		emptyLabel.setMaxWidth(15);
		emptyLabel.setMinWidth(15);
		GridPane.setMargin(pane, new Insets(0, 5, 0, 0));
		pane.add(emptyLabel, 0, 0);

		pane.focusedProperty().addListener((observableValue, aBoolean, aBoolean2) ->
		{
			if (!aBoolean)
			{
				pane.getStyleClass().removeAll(pane.getStyleClass());
				pane.getStyleClass().add(CssVariables.FOCUSED_EMPTY_GRID);
			}
			if (!aBoolean2)
			{
				pane.getStyleClass().removeAll(pane.getStyleClass());
				pane.getStyleClass().add(CssVariables.EMPTY_GRID);
			}
		});
		
		pane.setOnDragDetected(new DragDetector(() ->  
				{
					return Arrays.toString(children.stream()
							.filter(node -> node.getUserData() != null) 
							.map(node -> ((Parameter)node.getUserData()).getName())
							.collect(Collectors.toList())
							.toArray());
				})::onDragDetected);
		
		pane.setOnDragDropped(event ->
		{
			Dragboard dragboard = event.getDragboard();
			boolean b = false;
			if (dragboard.hasString())
			{
				String str = dragboard.getString();
				if (str != null && str.startsWith("[") && str.endsWith("]"))
				{
					String[] fields = str.substring(1, str.length() - 1).split(",");
					Common.tryCatch(() -> 
					{
						getMatrix().parameterInsert(this.matrixItem, -1, 
								Arrays.stream(fields)
								.map(i -> new Pair<ReadableValue, TypeMandatory>(new ReadableValue(i.trim()), TypeMandatory.Extra))
								.collect(Collectors.toList()));
					}, "Error on change parameters");
				}
				
				b = true;
			}
			event.setDropCompleted(b);
			event.consume();
		});

		pane.setOnDragOver(event ->
		{
			if (event.getGestureSource() != pane && event.getDragboard().hasString())
			{
				event.acceptTransferModes(TransferMode.MOVE);
			}
			event.consume();
		});

		pane.setOnContextMenuRequested(contextMenuHandler);
		
		return pane;
	}

	private ChangeListener<Boolean> createKeyChangeListener(Parameter par, Control finalKey, int index)
	{
		return (observable, oldValue, newValue) ->
		{
			if (!oldValue && newValue)
			{
				Common.tryCatch(fnc::call, "Error on select current row");
			}
			String oldText = par.getName();
			String newText = ((TextField) finalKey).getText();
			if (!newValue && oldValue && !Str.areEqual(oldText, newText))
			{
				Common.tryCatch(() -> getMatrix().parameterSetName(this.matrixItem, index, newText), "Error on change parameters");
			}
			if (!newValue && oldValue)
			{
				strech((TextField) finalKey);
			}
		};
	}

	private ChangeListener<Boolean> createExpressionChangeListener(Parameter par,  ExpressionField expressionField, int index)
	{
		return (observable, oldValue, newValue) ->
		{
			if (newValue && !oldValue)
			{
				Common.tryCatch(fnc::call,  "Error on select current row");
			}
			String oldText = par.getExpression();
			String newText = expressionField.getText();
			if (!newValue && oldValue/* && !Str.areEqual(oldText, newText)*/)
			{
				Common.tryCatch(() -> getMatrix().parameterSetValue(this.matrixItem, index, expressionField.getText()), "Error on change parameters");
			}
		};
	}

	private Pane parameterBox(Parameter par, int index, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		GridPane tempGrid = new ParameterGridPane();
		tempGrid.setUserData(par);
		Control key = new TextField(par.getName());
		((TextField) key).setPromptText("Key");
		Common.sizeTextField((TextField) key);
		final Control finalKey = key;
		key.focusedProperty().addListener(createKeyChangeListener(par, finalKey, index));
		switch (par.getType())
		{
			case Mandatory:
			case NotMandatory:
				key = new Label(par.getName());
				Common.sizeLabel((Label) key);
		}
		key.setContextMenu(empty);
		key.setOnContextMenuRequested(contextMenuHandler);
		tempGrid.add(key, 0, 0);
		GridPane.setMargin(key, Common.insetsNode);
		focusedParent(key);
		key.setStyle(Common.FONT_SIZE);
		
		if (this.generator != null)
		{
			key.setOnDragDetected(new DragDetector(() -> this.generator.generate() + par.getName())::onDragDetected);
		}
		
		if (!this.oneLine)
		{
			ExpressionField expressionField = new ExpressionField(this.context.getEvaluator(), par.getExpression());
			if (this.matrixItem instanceof ActionItem)
			{
				ActionItem actionItem = (ActionItem) this.matrixItem;
				HelpKind howHelp = null; 
						
				try
				{
					howHelp = actionItem.howHelpWithParameter(this.context, par.getName());
				}
				catch (Exception e)
				{ }

				AbstractEvaluator evaluator = this.context.getEvaluator();
				Settings settings = this.context.getConfiguration().getSettings();
				String themePath = Common.currentTheme().getPath();
				
				if (howHelp != null ) 
				{
					switch (howHelp)
					{
						case BuildQuery:
							break;
							
						case BuildLayoutExpression:
							expressionField.setNameFirst("↔");
							expressionField.setFirstActionListener(str -> 
							{
								String expression = this.parameters.getExpression(DialogFill.dialogName);
								String dialogName = null;
								try
								{
									dialogName = String.valueOf(evaluator.evaluate(expression));
								}
								catch (Exception e) {}
								try
								{
									LayoutExpressionBuilder viewer = new LayoutExpressionBuilder(par.getName(), expressionField.getText(), this.matrixItem.getParent().getMatrix().getDefaultApplicationConnection(), dialogName, evaluator);
									return viewer.show("Layout expression for " + par.getName(), false);
								}
								catch (Exception e)
								{
									DialogsHelper.showError(e.getMessage());
								}
								return  expressionField.getText();
							});
							break;
							
						case ChooseDateTime:
							expressionField.setNameFirst("D");
							expressionField.setFirstActionListener(str -> 
							{
								Date res = DialogsHelper.showDateTimePicker(null);
								LocalDateTime ldt = Common.convert(res);
								return String.format("DateTime.date(%d, %d, %d,  %d, %d, %d)",
										ldt.getYear(), ldt.getMonthValue(), ldt.getDayOfMonth(), ldt.getHour(), ldt.getMinute(), ldt.getSecond());
							});
							break;
							
						case ChooseOpenFile:
							expressionField.setNameFirst("…");
							expressionField.setFirstActionListener(str -> 
								{
									File file = DialogsHelper.showOpenSaveDialog("Choose file to open", "All files", "*.*", OpenSaveMode.OpenFile);
									if (file != null)
									{
										return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
									}
									return str;
								});
							break;
							
						case ChooseSaveFile:
							expressionField.setNameFirst("…");
							expressionField.setFirstActionListener(str -> 
								{
									File file = DialogsHelper.showOpenSaveDialog("Choose file to save", "All files", "*.*", OpenSaveMode.SaveFile);
									if (file != null)
									{
										return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
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
										return this.context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
									}
									return str;
								});
							break;
							
						case ChooseFromList:
							expressionField.setChooserForExpressionField(par.getName(), () ->
							{
								return actionItem.listToFillParameter(this.context, par.getName());
							});
							break;
							
						case BuildXPath:
							expressionField.setNameFirst("X");
							
							expressionField.setFirstActionListener(str -> 
							{
								for (int i = 0; i < this.parameters.size(); i++)
								{
									Parameter next = this.parameters.getByIndex(i);
									Object obj = evaluator.tryEvaluate(next.getExpression());
									if (obj instanceof Xml)
									{
										Xml xml = (Xml)obj;
										Object value = evaluator.tryEvaluate(par.getExpression());
										String initial = value == null ? null : String.valueOf(value);
										XpathViewer viewer = new XpathViewer(null, xml.getDocument(), null);
										String res = viewer.show(initial, "Xpath for " + par.getName(), themePath, false); //TODO themePath need replace to Common.currentTheme().getPath()
										if (res != null)
										{
											res = evaluator.createString(res);
										}
										
										return res;
									}
								}
								return str;
							});
							break;
							
						default:
							break;
					}
				}
			}
			expressionField.setContextMenu(empty);
			expressionField.setOnContextMenuRequested(contextMenuHandler);
			expressionField.setNotifierForErrorHandler();
			expressionField.setHelperForExpressionField(par.getName(), this.matrixItem.getMatrix());
			expressionField.setChangingValueListener(createExpressionChangeListener(par, expressionField, index));
			tempGrid.add(expressionField, 0, 1);
			GridPane.setMargin(expressionField, Common.insetsNode);
			focusedParent(expressionField);
		}
		tempGrid.setOnContextMenuRequested(contextMenuHandler);
		
		return tempGrid;
	}

	private void focusedParent(final Node node)
	{
		ChangeListener<Boolean> changeListener = (observableValue, aBoolean, aBoolean2) ->
		{
			node.getParent().getStyleClass().removeAll(CssVariables.UNFOCUSED_GRID, CssVariables.FOCUSED_FIELD);
			if (!aBoolean)
			{
				node.getParent().getStyleClass().add(CssVariables.FOCUSED_FIELD);
			}
			if (!aBoolean2)
			{
				node.getParent().getStyleClass().add(CssVariables.UNFOCUSED_GRID);
			}
		};
		if (node instanceof ExpressionField)
		{
			((ExpressionField) node).setChangingFocusListener(changeListener);
		}
		else if (node instanceof TextField)
		{
			node.focusedProperty().addListener(changeListener);
		}
		else if (node instanceof ComboBox)
		{
			((ComboBox<?>) node).getEditor().focusedProperty().addListener(changeListener);
		}
	}

	private void strech(TextField textField)
	{
		int size = textField.getText() != null ? (textField.getText().length() * 8 + 20) : 60;

		if (textField.getScene() != null)
		{
			double v = textField.getScene().getWindow().getWidth() / 3;
			if (size > v)
			{
				textField.setPrefWidth(v);
				return;
			}
		}
		textField.setPrefWidth(size);
	}


	private MatrixFx getMatrix()
	{
		return ((MatrixFx) this.matrixItem.getMatrix());
	}
}
