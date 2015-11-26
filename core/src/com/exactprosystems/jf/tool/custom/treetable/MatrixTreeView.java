package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.help.ActionsList;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.Configuration;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.parser.Tokens;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixItemState;
import com.exactprosystems.jf.common.parser.listeners.DummyRunnerListener;
import com.exactprosystems.jf.common.parser.listeners.IMatrixListener;
import com.exactprosystems.jf.common.parser.listeners.SilenceMatrixListener;
import com.exactprosystems.jf.common.report.ContextHelpFactory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.xml.control.Table;
import com.exactprosystems.jf.functions.Text;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.MatrixFx.PlaceToInsert;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import com.exactprosystems.jf.tool.settings.SettingsPanel;

import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Window;

import org.apache.log4j.Logger;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class MatrixTreeView extends TreeTableView<MatrixItem>
{
	public static boolean canShow = true;
	private MatrixFx 	matrix;

	public static final int NUMBER_COLUMN_WIDTH =	40;
	public static final int OFF_COLUMN_WIDTH =		25;
	public static final int ICON_COLUMN_WIDTH =		23;

	private ContextMenu addBeforeMenu;
	private ContextMenu addAfterMenu;
	private ContextMenu addChildMenu;


	private final static Logger	logger	= Logger.getLogger(MatrixTreeView.class);
	
	public MatrixTreeView()
	{
		super(null);
		this.setShowRoot(false);
		this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.getStyleClass().add(CssVariables.CUSTOM_TREE_TABLE_VIEW);
		initTable();
	}

	public void init(MatrixFx matrix, Settings settings)
	{
		this.matrix = matrix;
		addBeforeMenu = createInsertMenu(PlaceToInsert.Before);
		addAfterMenu = createInsertMenu(PlaceToInsert.After);
		addChildMenu = createInsertMenu(PlaceToInsert.Child);
		ContextMenu contextMenu = createContextMenu(settings);

		setRoot(new TreeItem<>(matrix.getRoot()));
		
		setRowFactory(treeView ->
		{
			try
			{
				MatrixTreeRow row = new MatrixTreeRow(contextMenu);
				shortCuts(row, settings);
				return row;
			}
			catch (Exception e)
			{
				String message = "Error on set cell factory\n" + e.getMessage();
				logger.error(message);
				logger.error(e.getMessage(), e);
				DialogsHelper.showError(message);
			}
			return new TreeTableRow<>();
		});
	}

	public void setCurrent(TreeItem<MatrixItem> treeItem)
	{
		if (treeItem != null)
		{
			Platform.runLater(() ->
			{
				TreeItem<MatrixItem> parent = treeItem.getParent();
				while (parent != null)
				{
					parent.setExpanded(true);
					parent = parent.getParent();
				}
				final int row = getRow(treeItem);
				getSelectionModel().clearSelection();
				getSelectionModel().select(treeItem);
				tryCatch(() -> Thread.sleep(100), "Error sleep");
				treeItem.setExpanded(true);
				scrollTo(row);
			});
		}
	}
	
	public void refresh()
	{
		Optional.ofNullable(this.getColumns().get(0)).ifPresent(col -> {
			col.setVisible(false);
			col.setVisible(true);
		});
	}
	
	public void refreshParameters(MatrixItem item)
	{
		TreeItem<MatrixItem> treeItem = find(item);
		if (treeItem != null)
		{
			GridPane layout = (GridPane)treeItem.getValue().getLayout();
			{
				layout.getChildren().stream().filter(pane -> pane instanceof ParametersPane).forEach(pane -> Platform.runLater(((ParametersPane) pane)::refreshParameters));
			}
		}
	}
	
	public void expandAll()
	{
		expand(getRoot(), true);
	}

	public void collapseAll()
	{
		expand(getRoot(), false);
	}

	public List<MatrixItem> currentItems()
	{
		return getSelectionModel().getSelectedItems().stream().map(TreeItem::getValue).collect(Collectors.toList());
	}

	public MatrixItem currentItem()
	{
		TreeItem<MatrixItem> selectedItem = getSelectionModel().getSelectedItem();
		MatrixItem item = selectedItem != null ? selectedItem.getValue() : null;
		if (item == null)
		{
			DialogsHelper.showInfo("Can't do current operation, because selected item is null");
		}
		return item;
	}

	public TreeItem<MatrixItem> find(MatrixItem item)
	{
		return find(this.getRoot(), item);
	}

	public TreeItem<MatrixItem> find(TreeItem<MatrixItem> parent, MatrixItem item)
	{
		return find(parent, matrixItem -> item == matrixItem);
	}

	public TreeItem<MatrixItem> find(Predicate<MatrixItem> strategy)
	{
		return find(this.getRoot(), strategy);
	}

	public TreeItem<MatrixItem> find(TreeItem<MatrixItem> parent, Predicate<MatrixItem> strategy)
	{
		if (strategy.test(parent.getValue()))
		{
			return parent;
		}
		for (TreeItem<MatrixItem> treeItem : parent.getChildren())
		{
			TreeItem<MatrixItem> itemTreeItem = find(treeItem, strategy);
			if (itemTreeItem != null)
			{
				return itemTreeItem;
			}
		}
		return null;
	}

	private void shortCuts(MatrixTreeRow row, final Settings settings)
	{
		setOnKeyPressed(keyEvent -> Common.tryCatch(() ->
		{
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				MatrixItem currentItem = currentItem();
				if (currentItem != null)
				{
					GridPane layout = (GridPane) currentItem.getLayout();
					layout.getChildren().stream().filter(n -> n instanceof GridPane).findFirst().ifPresent(p -> ((GridPane) p).getChildren().get(0).requestFocus());
				}
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SHOW_ALL))
			{
				row.showExpressionsResults();
			}
			else if (keyEvent.getCode() == KeyCode.ESCAPE)
			{
				MatrixTreeView.this.requestFocus();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.GO_TO_LINE))
			{
				int index = new GoToLine().show();
				if (index != -1)
				{
					TreeItem<MatrixItem> treeItem = find(matrixItem -> matrixItem.getNumber() == index);
					if (treeItem == null)
					{
						DialogsHelper.showError(String.format("Matrix item with number '%d' not found", index));
					}
					else
					{
						setCurrent(treeItem);
					}
				}
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.BREAK_POINT))
			{
				breakPoint();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.DELETE_ITEM))
			{
				deleteCurrentItems();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.COPY_ITEMS))
			{
				copyItems();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.PASTE_ITEMS))
			{
				pasteItems();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.HELP))
			{
				new ActionHelp();
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_BEFORE))
			{
				show(addBeforeMenu);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_AFTER))
			{
				show(addAfterMenu);
			}
			else if (SettingsPanel.match(settings, keyEvent, SettingsPanel.ADD_CHILD))
			{
				show(addChildMenu);
			}
		}, "Error on do actions by shortcuts"));

		setOnKeyReleased(keyEvent -> Common.tryCatch(() ->
		{
			if (SettingsPanel.match(settings, keyEvent, SettingsPanel.SHOW_ALL))
			{
				row.hideExpressionsResults();
			}
		}, "Error on hide all"));
	}

	private void initTable()
	{
		this.setEditable(true);
		TreeTableColumn<MatrixItem, Integer> numberColumn = new TreeTableColumn<>();
		numberColumn.setSortable(false);
		numberColumn.setMinWidth(40);
		numberColumn.setPrefWidth(40);
		numberColumn.setMaxWidth(41);
		numberColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue().getNumber()));

		TreeTableColumn<MatrixItem, MatrixItemState> iconColumn = new TreeTableColumn<>();
		iconColumn.setSortable(false);
		iconColumn.setMinWidth(23);
		iconColumn.setPrefWidth(23);
		iconColumn.setMaxWidth(24);
		iconColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue().getItemState()));
		iconColumn.setCellFactory(value -> new IconCell());

		TreeTableColumn<MatrixItem, MatrixItem> gridColumn = new TreeTableColumn<>();
		gridColumn.setSortable(false);
		gridColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));
		gridColumn.setCellFactory(param -> new MatrixItemCell());

		TreeTableColumn<MatrixItem, MatrixItem> offColumn = new TreeTableColumn<>();
		offColumn.setSortable(false);
		offColumn.setMinWidth(25);
		offColumn.setMaxWidth(26);
		offColumn.setPrefWidth(25);
		offColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));
		offColumn.setCellFactory(p -> new TreeTableCell<MatrixItem, MatrixItem>()
		{
			private CheckBox box = new CheckBox();

			@Override
			protected void updateItem(MatrixItem item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null)
				{
					box.setSelected(item.isOff());
					box.setOnAction(event -> 
					{ 
						matrix.setOff(item.getNumber(), box.isSelected());
						refresh();
					});
					setGraphic(box);
				}
				else
				{
					setGraphic(null);
				}
			}
		});
		offColumn.setEditable(true);

		this.treeColumnProperty().set(gridColumn);
		this.getColumns().add(numberColumn);
		this.getColumns().add(offColumn);
		this.getColumns().add(iconColumn);
		this.getColumns().add(gridColumn);
		gridColumn.setMaxWidth(Double.MAX_VALUE);
		gridColumn.prefWidthProperty().bind(this.widthProperty().subtract(numberColumn.getWidth() + iconColumn.getWidth() + offColumn.getWidth()).subtract(2));
	}

	private void breakPoint()
	{
		Common.tryCatch(() -> this.matrix.breakPoint(currentItems()), "Error on breakpoint");
	}

	private void deleteCurrentItems()
	{
		Common.tryCatch(() -> this.matrix.remove(currentItems()), "Error on delete item");
	}

	private void copyItems()
	{
		Common.tryCatch(() -> this.matrix.copy(currentItems()), "Error on copy");
	}

	private void pasteItems()
	{
		Common.tryCatch(() -> this.matrix.paste(currentItem()), "Error on paste");
	}

	private ContextMenu createContextMenu(Settings settings)
	{
		ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);

		MenuItem breakPoint = new MenuItem("Breakpoint");
		breakPoint.setGraphic(new ImageView(new Image(CssVariables.Icons.BREAK_POINT_ICON)));
		breakPoint.setOnAction(event -> breakPoint());

		MenuItem addBefore = new MenuItem("Add before");
		addBefore.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_BEFORE_ICON)));
		addBefore.setOnAction(event -> Common.tryCatch(() -> show(addBeforeMenu), "Error on add before"));

		MenuItem addAfter = new MenuItem("Add after");
		addAfter.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_AFTER_ICON)));
		addAfter.setOnAction(event -> Common.tryCatch(() -> show(addAfterMenu), "Error on add after"));

		MenuItem addChild = new MenuItem("Add child");
		addChild.setGraphic(new ImageView(new Image(CssVariables.Icons.ADD_CHILD_ICON)));
		addChild.setOnAction(event -> Common.tryCatch(() -> show(addChildMenu), "Error on add child"));

		MenuItem deleteItem = new MenuItem("Delete");
		deleteItem.setGraphic(new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		deleteItem.setOnAction(event -> deleteCurrentItems());

		MenuItem copy = new MenuItem("Copy");
		copy.setGraphic(new ImageView(new Image(CssVariables.Icons.COPY_ICON)));
		copy.setOnAction(event -> copyItems());
		
		MenuItem paste = new MenuItem("Paste");
		paste.setGraphic(new ImageView(new Image(CssVariables.Icons.PASTE_ICON)));
		paste.setOnAction(event -> pasteItems());

		MenuItem help = new MenuItem("Help");
		help.setGraphic(new ImageView(new Image(CssVariables.Icons.HELP_ICON)));
		help.setOnAction(new ActionHelp());

		contextMenu.getItems().addAll(
				breakPoint,
				new SeparatorMenuItem(),
				copy,
				paste,
				new SeparatorMenuItem(),
				addAfter,
				addChild,
				addBefore,
				deleteItem,
				new SeparatorMenuItem(),
				help
			);

		return contextMenu;
	}

	private void show(ContextMenu menu)
	{
		Window window = Common.getTabPane().getScene().getWindow();
		Point2D windowCoord = new Point2D(window.getX(), window.getY());
		final double x = windowCoord.getX() + window.getWidth() / 2;
		final double y = windowCoord.getY() + window.getHeight() / 2;
		show(menu, x, y);
	}

	private void show(ContextMenu menu, double x, double y)
	{
		if (canShow)
		{
			canShow = false;
			Window sceneWindow = getSceneWindow();
			if (sceneWindow == null)
			{
				menu.show(Common.node, x, y);
			}
			else
			{
				menu.show(sceneWindow, x, y);
			}
		}
	}

	private Window getSceneWindow()
	{
		Scene scene = getScene();
		if (scene == null)
		{
			return null;
		}
		
		return scene.getWindow();
	}

	private ContextMenu createInsertMenu(PlaceToInsert placeToInsert)
	{
		ContextMenu insertMenu = new ContextMenu();
		insertMenu.setAutoHide(true);
		insertMenu.setHideOnEscape(true);
		insertMenu.setAutoHide(true);
		insertMenu.setOnHiding(e -> canShow = true);

		Menu actionItemMenu = new Menu("ActionItem");
		
		Map<ActionGroups, Menu> map = new HashMap<>();
		Arrays.asList(ActionGroups.values()).stream().forEach(group ->
		{
			Menu menu = new Menu(group.name());
			actionItemMenu.getItems().add(menu);
			map.put(group, menu);
		});
		
		Arrays.asList(ActionsList.actions).forEach(clazz ->
		{
			MenuItem menuItem = new MenuItem(clazz.getSimpleName());
			menuItem.setOnAction(event -> Common.tryCatch(() -> {

				MatrixItem item = currentItem();
				if (item != null)
				{
					this.matrix.insertNew(item, placeToInsert, Tokens.Action.get(), clazz.getSimpleName());
					if (placeToInsert.equals(PlaceToInsert.Child))
					{
						find(item).setExpanded(true);
					}
				}
			}, "Error on create item"));
			ActionGroups aClassGroup = clazz.getAnnotation(ActionAttribute.class).group();
			map.get(aClassGroup).getItems().add(menuItem);
		});
		
		Menu dataItemMenu = new Menu("Raw data");
		MenuItem insertDataTable = new MenuItem(Table.class.getSimpleName());
		insertDataTable.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = currentItem();
			matrix.insertNew(item, placeToInsert, Tokens.RawTable.get(), Table.class.getSimpleName());
			if (placeToInsert.equals(PlaceToInsert.Child))
			{
				find(item).setExpanded(true);
			}
		}, "Error on insert") );
		
		MenuItem insertDataMessage = new MenuItem(MapMessage.class.getSimpleName());
		insertDataMessage.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = currentItem();
			matrix.insertNew(item, placeToInsert, Tokens.RawMessage.get(), "none");
			if (placeToInsert.equals(PlaceToInsert.Child))
			{
				find(item).setExpanded(true);
			}
		}, "Error on insert") );
		
		MenuItem insertDataText = new MenuItem(Text.class.getSimpleName());
		insertDataText.setOnAction(event -> Common.tryCatch(() -> 
		{
			MatrixItem item = currentItem();
			matrix.insertNew(item, placeToInsert, Tokens.RawText.get(), null);
			if (placeToInsert.equals(PlaceToInsert.Child))
			{
				find(item).setExpanded(true);
			}
		}, "Error on insert") );
		
		dataItemMenu.getItems().addAll(
				insertDataTable,
				insertDataMessage,
				insertDataText
			);
		
		insertMenu.getItems().addAll(
				actionItemMenu, 
				dataItemMenu,
				new MenuItem(Tokens.TestCase.get()), 
				new MenuItem(Tokens.SubCase.get()), 
				new MenuItem(Tokens.Return.get()), 
				new MenuItem(Tokens.Call.get()), 
				new MenuItem(Tokens.If.get()), 
				new MenuItem(Tokens.Else.get()), 
				new MenuItem(Tokens.For.get()), 
				new MenuItem(Tokens.ForEach.get()), 
				new MenuItem(Tokens.While.get()), 
				new MenuItem(Tokens.Continue.get()), 
				new MenuItem(Tokens.Break.get()), 
				new MenuItem(Tokens.OnError.get()), 
				new MenuItem(Tokens.Switch.get()), 
				new MenuItem(Tokens.Case.get()), 
				new MenuItem(Tokens.Default.get()), 
				new MenuItem(Tokens.ReportOn.get()), 
				new MenuItem(Tokens.ReportOff.get()), 
				new MenuItem(Tokens.Fail.get())
			);

		insertMenu.getItems()
			.stream()
			.filter(menuItem -> !(menuItem instanceof Menu))
			.forEach(menuItem -> menuItem.setOnAction(event -> Common.tryCatch(() -> {
				MatrixItem item = currentItem();
				if (item != null)
				{
					matrix.insertNew(item, placeToInsert, menuItem.getText(), null);
					if (placeToInsert.equals(PlaceToInsert.Child))
					{
						find(item).setExpanded(true);
					}
				}
			}, "Error on add matrix item")));

		return insertMenu;
	}

	private void expand(TreeItem<MatrixItem> rootItem, boolean flag)
	{
		rootItem.getChildren().forEach(item ->
		{
			item.setExpanded(flag);
			expand(item, flag);
		});
	}

	private class ActionHelp implements EventHandler<ActionEvent>
	{
		public ActionHelp()
		{
		}

		@Override
		public void handle(ActionEvent actionEvent)
		{
			Configuration configuration = new Configuration();
			IMatrixListener dummy = new SilenceMatrixListener();
			try (Context context = new Context(dummy, new DummyRunnerListener(), null, configuration))
			{
				TreeItem<MatrixItem> currentItem = getSelectionModel().getSelectedItem();
				if (currentItem != null)
				{
					MatrixItem item = currentItem.getValue();

					ReportBuilder report = new ContextHelpFactory().createBuilder(null, null, new Date());

					item.documentationOnlyThis(context, report);

					WebView browser = new WebView();
					WebEngine engine = browser.getEngine();
					String str = report.getContent();
					engine.loadContent(str);

					Dialog<?> dialog = new Alert(Alert.AlertType.INFORMATION);
					dialog.getDialogPane().setContent(browser);
					dialog.getDialogPane().setPrefWidth(1024);
					dialog.getDialogPane().setPrefHeight(768);
					dialog.setResizable(true);
					dialog.setHeaderText("Help for " + item.getItemName());
					dialog.setTitle("HELP");
					dialog.getDialogPane().getStylesheets().addAll(Common.currentTheme().getPath());
					dialog.show();
				}
			}
			catch (Exception e)
			{
				String message = "Error on show result";
				logger.error(message);
				logger.error(e.getMessage(), e);
				DialogsHelper.showError(message);
			}
		}
	}

	private class GoToLine
	{

		public int show()
		{
			TextInputDialog dialog = new TextInputDialog();
			dialog.getDialogPane().getStylesheets()
					.add(Common.currentTheme().getPath());
			dialog.getDialogPane().setHeader(new Pane());
			dialog.setTitle("Enter line number");
			dialog.getEditor()
					.textProperty()
					.addListener(
							(observable, oldValue, newValue) ->
							{
								if (!newValue.isEmpty())
								{
									if (!newValue
											.matches(Common.intPositiveNumberMatcher))
									{
										dialog.getEditor().setText(oldValue);
									}
								}
							});
			Optional<String> string = dialog.showAndWait();
			if (string.isPresent())
			{
				try
				{
					return Integer.parseInt(string.get());
				} catch (NumberFormatException e)
				{
					//
				}
			}
			return -1;
		}
	}
}
