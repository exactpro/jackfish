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
import com.exactprosystems.jf.api.common.DateTime;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.FormulaGenerator;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
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
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.RowConstraints;
import javafx.util.Pair;

import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class ParametersPane extends CustomScrollPane
{
	private static final int oneLineHeight = 40;
	private static final int rowHeight = 24;
	private static final int twoLineHeight = 63;

	private GridPane mainGridPane;
	private Context context;
	private Parameters parameters;
	private MatrixItem matrixItem;
	private boolean oneLine;
	private FormulaGenerator generator;
	private EventHandler<ContextMenuEvent> contextMenuHandler;
	private Common.Function fnc;
	private static ContextMenu empty = new ContextMenu();

	public ParametersPane(MatrixItem matrixItem, Context context, boolean oneLine, Parameters parameters, FormulaGenerator generator, MatrixContextMenu rowContextMenu, MatrixParametersContextMenu parametersContextMenu, Common.Function fnc)
	{
		super(oneLine ? oneLineHeight : twoLineHeight);

		this.mainGridPane = new GridPane();
		this.mainGridPane.getStyleClass().add(CssVariables.PARAMETERS_PANE);
		this.mainGridPane.setGridLinesVisible(true);
		this.mainGridPane.getRowConstraints().add(createRow(4));
		this.mainGridPane.getRowConstraints().add(createRow(rowHeight));
		if (!oneLine)
		{
			this.mainGridPane.getRowConstraints().add(createRow(rowHeight));
		}

		this.mainGridPane.getRowConstraints().add(createRow(4));

		this.fnc = fnc;
		this.setContent(this.mainGridPane);
		this.matrixItem = matrixItem;
		this.context = context;
		this.oneLine = oneLine;
		this.parameters = parameters;
		this.generator = generator;

		this.contextMenuHandler = parametersContextMenu.createContextMenuHandler();

		super.setContextMenu(rowContextMenu);
		refreshParameters(-1);
	}

	private RowConstraints createRow(int height)
	{
		RowConstraints row = new RowConstraints();
		row.setMaxHeight(height);
		row.setPrefHeight(height);
		row.setMinHeight(height);
		return row;
	}

	public void refreshParameters(int selectedIndex)
	{
		ObservableList<Node> children = FXCollections.observableArrayList(this.mainGridPane.getChildren());
		this.mainGridPane.getChildren().clear();

		for (int i = 0; i < this.parameters.size(); i++)
		{
			Parameter par = this.parameters.getByIndex(i);
			ParameterGridPane exist = findAndRemovePane(children, par);

			if (exist == null)
			{
				exist = parameterBox(par, this.contextMenuHandler);
			}
			exist.updateIndex(this.parameters.getIndex(par));
			if (i == selectedIndex)
			{
				exist.focusParameter();
			}

			this.mainGridPane.add(exist, i + 3, 1, 1, this.oneLine ? 1 : 2);
		}
		this.mainGridPane.add(Common.createSpacer(Common.SpacerEnum.HorizontalMid), 0, 0, 1, 2);
		this.mainGridPane.add(emptyBox(FXCollections.observableArrayList(this.mainGridPane.getChildren()), this.contextMenuHandler), 1, 1, 1, oneLine ? 1 : 2);
		this.mainGridPane.add(Common.createSpacer(Common.SpacerEnum.HorizontalMin), 2, 0, 1, 2);
		this.mainGridPane.add(Common.createSpacer(Common.SpacerEnum.VerticalMin), 0, 3);
		this.mainGridPane.add(Common.createSpacer(Common.SpacerEnum.VerticalMin), 0, 0);

		if (!oneLine)
		{
			for (Node child : children)
			{
				if (child instanceof ParameterGridPane)
				{
					((ParameterGridPane) child).getExpressionField().clearlListener();
				}
			}
		}
	}

	private ParameterGridPane findAndRemovePane(ObservableList<Node> children, Parameter par)
	{
		Iterator<Node> iter = children.iterator();
		while (iter.hasNext())
		{
			Node node = iter.next();
			if (node instanceof ParameterGridPane)
			{
				if (((ParameterGridPane) node).getParameter() == par)
				{
					iter.remove();
					return (ParameterGridPane) node;
				}
			}
		}
		return null;
	}

	private Pane emptyBox(ObservableList<Node> children, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		GridPane pane = new EmptyParameterGridPane();
		pane.getStyleClass().add(CssVariables.EMPTY_GRID);

		Label emptyLabel = new Label();
		emptyLabel.getStyleClass().add(CssVariables.INVISIBLE_FIELD);
		emptyLabel.setPrefWidth(12);
		emptyLabel.setMaxWidth(12);
		emptyLabel.setMinWidth(12);
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

		pane.setOnDragDetected(new DragDetector(() -> Arrays.toString(children.stream().filter(node -> node instanceof ParameterGridPane).map(node -> ((ParameterGridPane) node).getParameter()).filter(Objects::nonNull).map(Parameter::getName).collect(Collectors.toList()).toArray()))::onDragDetected);

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
					Common.tryCatch(() -> getMatrix().parameterInsert(this.matrixItem, -1, Arrays.stream(fields).map(i -> new Pair<>(new ReadableValue(i.trim()), TypeMandatory.Extra)).collect(Collectors.toList())), "Error on change parameters");
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

	private ParameterGridPane parameterBox(Parameter par, EventHandler<ContextMenuEvent> contextMenuHandler)
	{
		ParameterGridPane tempGrid = new ParameterGridPane(this.parameters.getIndex(par), par);
		Control key = new TextField(par.getName());
		((TextField) key).setPromptText("Key");
		Common.sizeTextField((TextField) key);
		switch (par.getType())
		{
			case Mandatory:
			case NotMandatory:
				key = new Label(par.getName());
				Common.sizeLabel((Label) key);
		}
		tempGrid.setKey(key);
		tempGrid.setKeyListener(this.fnc, (index, text) -> getMatrix().parameterSetName(this.matrixItem, index, text));
		key.setContextMenu(empty);
		key.setOnContextMenuRequested(contextMenuHandler);
		focusedParent(key);
		key.setStyle(Common.FONT_SIZE);

		if (this.generator != null)
		{
			key.setOnDragDetected(new DragDetector(() -> this.generator.generate() + par.getName())::onDragDetected);
		}

		if (!this.oneLine)
		{
			ExpressionField expressionField = new ExpressionField(this.context.getEvaluator(), par.getExpression());
			expressionField.setStretchable(true);
			if (this.matrixItem instanceof ActionItem)
			{
				ActionItem actionItem = (ActionItem) this.matrixItem;
				HelpKind howHelp = null;

				try
				{
					howHelp = actionItem.howHelpWithParameter(this.context, par.getName());
				}
				catch (Exception e)
				{
				}

				AbstractEvaluator evaluator = this.context.getEvaluator();
				List<String> themePaths = Common.currentThemesPaths();

				if (howHelp != null)
				{
					expressionField.setNameFirst(howHelp.getLabel());
					switch (howHelp)
					{
						case BuildQuery:
							break;

						case BuildLayoutExpression:
							expressionField.setFirstActionListener(str ->
							{
								String expression = this.parameters.getExpression(DialogFill.dialogName);
								String dialogName = null;
								try
								{
									dialogName = String.valueOf(evaluator.evaluate(expression));
								}
								catch (Exception e)
								{
								}
								try
								{
									LayoutExpressionBuilder viewer = new LayoutExpressionBuilder(par.getName(), expressionField.getText(), this.matrixItem.getParent().getMatrix().getDefaultApplicationConnection(), dialogName, evaluator);
									return viewer.show("Layout expression for " + par.getName(), false);
								}
								catch (Exception e)
								{
									DialogsHelper.showError(e.getMessage());
								}
								return expressionField.getText();
							});
							break;

						case ChooseDateTime:
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
							expressionField.setChooserForExpressionField(par.getName(), () -> actionItem.listToFillParameter(this.context, par.getName()));
							break;

						case BuildXPath:
							expressionField.setFirstActionListener(str ->
							{
								for (int i = 0; i < this.parameters.size(); i++)
								{
									Parameter next = this.parameters.getByIndex(i);
									Object obj = evaluator.tryEvaluate(next.getExpression());
									if (obj instanceof Xml)
									{
										Xml xml = (Xml) obj;
										Object value = evaluator.tryEvaluate(par.getExpression());
										String initial = value == null ? null : String.valueOf(value);
										XpathViewer viewer = new XpathViewer(null, xml.getDocument(), null);
										String res = viewer.show(initial, "Xpath for " + par.getName(), themePaths, false);
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
			tempGrid.setValue(expressionField);
			tempGrid.setValueListener(this.fnc, (index, text) -> getMatrix().parameterSetValue(this.matrixItem, index, text));
			focusedParent(expressionField);
		}
		tempGrid.setOnContextMenuRequested(contextMenuHandler);

		return tempGrid;
	}

	private void focusedParent(final Node node)
	{
		ChangeListener<Boolean> changeListener = (observableValue, aBoolean, aBoolean2) ->
		{
			node.getParent().getStyleClass().removeAll(CssVariables.FOCUSED_PARAMETER_GRID_PANE);
			if (!aBoolean)
			{
				node.getParent().getStyleClass().add(CssVariables.FOCUSED_PARAMETER_GRID_PANE);
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

	private MatrixFx getMatrix()
	{
		return ((MatrixFx) this.matrixItem.getMatrix());
	}
}
