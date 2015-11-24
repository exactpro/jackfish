package com.exactprosystems.jf.tool.matrix.params;

import com.exactprosystems.jf.actions.ReadableValue;
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
import com.exactprosystems.jf.tool.Common.Function;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.DragDetector;
import com.exactprosystems.jf.tool.custom.expfield.ExpressionField;
import com.exactprosystems.jf.tool.custom.scroll.CustomScrollPane;
import com.exactprosystems.jf.tool.custom.treetable.MatrixTreeRow;
import com.exactprosystems.jf.tool.custom.xpath.XpathViewer;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.helpers.DialogsHelper.OpenSaveMode;
import com.exactprosystems.jf.tool.main.Main;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.exactprosystems.jf.tool.settings.Theme;

import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.util.Pair;

import java.awt.*;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ParametersPane extends CustomScrollPane
{
	private GridPane	mainGridPane;
	private Context		context;
	private Parameters	parameters;
	private MatrixItem	matrixItem;
	private boolean 	oneLine;
	private GridPane	selectedPane;
	private FormulaGenerator generator;

	public ParametersPane(MatrixItem matrixItem, Context context, boolean oneLine, Parameters parameters, FormulaGenerator generator)
	{
		super(oneLine ? 30 : 65);
		this.mainGridPane = new GridPane();
		this.setContent(this.mainGridPane);
		this.matrixItem = matrixItem;
		this.context = context;
		this.oneLine = oneLine;
		this.parameters = parameters;
		this.generator = generator;

		refreshParameters();
		createContextMenu();
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
				exist = parameterBox(par, i);
			}

			this.mainGridPane.add(exist, i + 1, 0, 1, this.oneLine ? 1 : 2);
		}
		this.mainGridPane.add(emptyBox(FXCollections.observableArrayList(this.mainGridPane.getChildren())), 0, 0, 1, 2);
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

	private Pane emptyBox(ObservableList<Node> children)
	{
		GridPane pane = new GridPane();
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
					changeParameters(() -> getMatrix().parameterInsert(this.matrixItem, 0, 
							Arrays.stream(fields)
							.map(i -> new Pair<ReadableValue, TypeMandatory>(new ReadableValue(i.trim()), TypeMandatory.Extra))
							.collect(Collectors.toList())));
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

		
		return pane;
	}

	private void strech(TextField tf)
	{
		int size = tf.getText() != null ? (tf.getText().length() * 8 + 20) : 60;

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

	private Pane parameterBox(Parameter par, int index)
	{
		GridPane tempGrid = new GridPane();
		tempGrid.setUserData(par);
		Control key = new TextField(par.getName());
		((TextField) key).setPromptText("Key");
		Common.sizeTextField((TextField) key);
		final Control finalKey = key;
		key.focusedProperty().addListener((observable, oldValue, newValue) ->
		{
			String oldText = par.getName();
			String newText = ((TextField) finalKey).getText();
			if (!newValue && oldValue && !Str.areEqual(oldText, newText))
			{
				changeParameters(() -> getMatrix().parameterSetName(this.matrixItem, index, newText));
			}
			if (!newValue && oldValue)
			{
				strech((TextField) finalKey);
			}
		});
		switch (par.getType())
		{
			case Mandatory:
			case NotMandatory:
				key = new Label(par.getName());
				Common.sizeLabel((Label) key);
		}
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
			ExpressionField expressionField = new ExpressionField(context.getEvaluator(), par.getExpression());
			if (matrixItem instanceof ActionItem)
			{
				ActionItem actionItem = (ActionItem) this.matrixItem;
				HelpKind howHelp = null; 
						
				try
				{
					howHelp = actionItem.howHelpWithParameter(context, par.getName());
				}
				catch (Exception e)
				{ }
				
				if (howHelp != null ) 
				{
					switch (howHelp)
					{
						case BuildQuery:
							
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
										return context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
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
										return context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
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
										return context.getEvaluator().createString(Common.getRelativePath(file.getAbsolutePath()));
									}
									return str;
								});
							break;
							
						case ChooseFromList:
							expressionField.setChooserForExpressionField(par.getName(), () ->
							{
								return actionItem.listToFillParameter(context, par.getName());
							});
							break;
							
						case BuildXPath:
							expressionField.setNameFirst("X");
							AbstractEvaluator evaluator = this.context.getEvaluator();
							Settings settings = this.context.getConfiguration().getSettings();
							Settings.SettingsValue theme = settings.getValueOrDefault(Settings.GLOBAL_NS, SettingsPanel.SETTINGS, Main.THEME, Theme.WHITE.name());
							String themePath = Theme.valueOf(theme.getValue()).getPath();
							
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
										String res = viewer.show(initial, "Xpath for " + par.getName(), themePath, false);
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
			expressionField.setNotifierForErrorHandler();
			expressionField.setHelperForExpressionField(par.getName(), this.matrixItem.getMatrix());
			expressionField.setChangingValueListener((observable, oldValue, newValue) ->
			{
				if (!newValue && oldValue)
				{
					changeParameters(() -> getMatrix().parameterSetValue(this.matrixItem, index, expressionField.getText()));
				}
			});
			tempGrid.add(expressionField, 0, 1);
			GridPane.setMargin(expressionField, Common.insetsNode);
			focusedParent(expressionField);
		}
		tempGrid.setOnContextMenuRequested(event -> this.selectedPane = tempGrid);
		
		return tempGrid;
	}

	private void addNewGrid(int index)
	{
		changeParameters(() -> this.getMatrix().parameterInsert(this.matrixItem, index));
	}

	protected void moveLeft(int index)
	{
		changeParameters(() -> this.getMatrix().parameterMoveLeft(this.matrixItem, index));
	}

	protected void moveRight(int index)
	{
		changeParameters(() -> this.getMatrix().parameterMoveRight(this.matrixItem, index));
	}

	protected void focusedParent(final Node node)
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

		if (node instanceof TextField)
		{
			node.focusedProperty().addListener(changeListener);
		}
		else if (node instanceof ComboBox)
		{
			((ComboBox<?>) node).getEditor().focusedProperty().addListener(changeListener);
		}
		else if (node instanceof ExpressionField)
		{
			((ExpressionField) node).setChangingFocusListener(changeListener);
		}
	}

	private void createContextMenu()
	{
		MenuItem menuItemRemove = new MenuItem("Remove");
		menuItemRemove.setGraphic(new ImageView(new Image(CssVariables.Icons.REMOVE_PARAMETER_ICON)));

		menuItemRemove.setOnAction(event -> changeParameters(() -> this.getMatrix().parameterRemove(this.matrixItem, selectedIndex())));
		menuItemRemove.setDisable(false);

		MenuItem menuItemMoveLeft = new MenuItem("Move to left");
		menuItemMoveLeft.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_LEFT_ICON)));
		menuItemMoveLeft.setOnAction(actionEvent -> moveLeft(selectedIndex()));
		menuItemMoveLeft.setDisable(false);

		MenuItem menuItemMoveRight = new MenuItem("Move to right");
		menuItemMoveRight.setGraphic(new ImageView(new Image(CssVariables.Icons.MOVE_RIGHT_ICON)));
		menuItemMoveRight.setOnAction(actionEvent -> moveRight(selectedIndex()));
		menuItemMoveRight.setDisable(false);

		MenuItem menuItemAdd = new MenuItem("Add param");
		menuItemAdd.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_PARAMETER_ICON)));
		menuItemAdd.setOnAction(event -> addNewGrid(selectedIndex() + 1));

		MenuItem menuItemShowAllParameters = new MenuItem("All parameters");
		menuItemShowAllParameters.setGraphic(new ImageView(new Image(CssVariables.Icons.ALL_PARAMETERS_ICON)));
		menuItemShowAllParameters.setOnAction(event -> Common.tryCatch(() ->
		{
			ActionItem actionItem = ((ActionItem) this.matrixItem);
			int fromIndex = selectedIndex() + 1;
			Map<ReadableValue, TypeMandatory> map = actionItem.helpToAddParameters(this.context);
			ShowAllParams params = new ShowAllParams(map, parameters, this.matrixItem.getItemName());
			ArrayList<Pair<ReadableValue, TypeMandatory>> result = params.show();
			
			getMatrix().parameterInsert(this.matrixItem, fromIndex, result);
		}, "Error on show all parameters"));
		menuItemShowAllParameters.setDisable(!(this.matrixItem instanceof ActionItem));

		ObservableList<MenuItem> gridItems = FXCollections.observableArrayList(
				menuItemRemove, 
				menuItemMoveLeft, 
				menuItemMoveRight, 
				menuItemAdd,
				new SeparatorMenuItem(), 
				menuItemShowAllParameters);

		this.setOnContextMenuRequested(event ->
		{
			Parent parent = getParent().getParent().getParent();
			if (parent instanceof MatrixTreeRow)
			{
				MatrixTreeRow cell = (MatrixTreeRow) parent;
				ObservableList<MenuItem> cellItems = cell.getContextMenu().getItems();
				IntStream.range(0, gridItems.size()).forEach(i -> cellItems.add(i, gridItems.get(i)));
				int selectedIndex = selectedIndex();
				Arrays.asList(menuItemMoveRight, menuItemMoveLeft).stream().forEach(item -> item.setDisable(!this.parameters.canMove(selectedIndex)));
				menuItemRemove.setDisable(!this.parameters.canRemove(selectedIndex));
				menuItemShowAllParameters.setDisable(!(this.matrixItem instanceof ActionItem));
				focusGrid(true);

				cell.getContextMenu().setOnHidden(e ->
				{
					focusGrid(false);
					this.selectedPane = null;
					cellItems.removeAll(gridItems);
				});
				cell.getContextMenu().show(this, MouseInfo.getPointerInfo().getLocation().getX(), MouseInfo.getPointerInfo().getLocation().getY());
			}
		});
	}

	private void changeParameters(Function fnc)
	{
		Common.tryCatch(() -> 
		{
			fnc.call();
		}, "Error on change parameters");
	}

	private void focusGrid(boolean flag)
	{
		Optional.ofNullable(selectedPane).ifPresent(pane ->
		{
			pane.getStyleClass().removeAll(CssVariables.UNFOCUSED_GRID, CssVariables.FOCUSED_FIELD);
			if (flag)
			{
				pane.getStyleClass().add(CssVariables.FOCUSED_FIELD);
			}
			else
			{
				pane.getStyleClass().add(CssVariables.UNFOCUSED_GRID);
			}
		});
	}

	private int selectedIndex()
	{
		if (selectedPane == null)
		{
			return -1;
		}
		else
		{
			return this.mainGridPane.getChildren().indexOf(selectedPane) - 1;
		}
	}

	private MatrixFx getMatrix()
	{
		return ((MatrixFx) this.matrixItem.getMatrix());
	}
}
