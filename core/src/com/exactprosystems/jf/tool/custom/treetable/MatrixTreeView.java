////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import com.exactprosystems.jf.common.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import com.exactprosystems.jf.tool.matrix.MatrixFx;
import com.exactprosystems.jf.tool.matrix.params.ParametersPane;
import com.exactprosystems.jf.tool.settings.SettingsPanel;
import com.sun.javafx.scene.control.skin.TreeTableViewSkin;
import javafx.application.Platform;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.GridPane;
import org.apache.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class MatrixTreeView extends TreeTableView<MatrixItem>
{
	private MatrixFx matrix;

	private final static Logger logger = Logger.getLogger(MatrixTreeView.class);

	public MatrixTreeView()
	{
		super(null);
		this.setSkin(new MatrixTreeViewSkin(this));
		this.setShowRoot(false);
		this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.getStyleClass().add(CssVariables.CUSTOM_TREE_TABLE_VIEW);
		initTable();
	}

	public void init(MatrixFx matrix, Settings settings, ContextMenu contextMenu)
	{
		this.matrix = matrix;

		setRoot(new TreeItem<>(matrix.getRoot()));

		setRowFactory(treeView -> {
			try
			{
				MatrixTreeRow row = new MatrixTreeRow(contextMenu);
				shortCuts(row, settings);
				return row;
			}
			catch (Exception e)
			{
				String message = "Error on set cell factory\n" + e.getMessage();
				logger.error(message, e);
				DialogsHelper.showError(message);
			}
			return new TreeTableRow<>();
		});
	}

	public void setCurrent(TreeItem<MatrixItem> treeItem)
	{
		if (treeItem != null)
		{
			Platform.runLater(() -> {
				TreeItem<MatrixItem> parent = treeItem.getParent();
				while (parent != null)
				{
					parent.setExpanded(true);
					parent = parent.getParent();
				}
				final int row = getRow(treeItem);
				getSelectionModel().clearAndSelect(row);
				tryCatch(() -> Thread.sleep(100), "Error sleep");
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

	public void refreshParameters(MatrixItem item, int selectedIndex)
	{
		TreeItem<MatrixItem> treeItem = find(item);
		if (treeItem != null)
		{
			GridPane layout = (GridPane) treeItem.getValue().getLayout();
			{
				layout.getChildren().stream().filter(pane -> pane instanceof ParametersPane).forEach(pane -> Platform.runLater(() -> {
					((ParametersPane) pane).refreshParameters(selectedIndex);
				}));
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
		return getSelectionModel().getSelectedCells().stream().map(TreeTablePosition::getTreeItem).map(TreeItem::getValue).collect(Collectors.toList());
	}

	public MatrixItem currentItem()
	{
		TreeItem<MatrixItem> selectedItem = getSelectionModel().getSelectedItem();
		MatrixItem item = selectedItem != null ? selectedItem.getValue() : null;
		return item;
	}

	//	public MatrixTreeRow currentRow()
	//	{
	//		// TODO Auto-generated method stub
	//		TreeItem<MatrixItem> selectedItem = getSelectionModel().getSelectedItem();
	//		getSelectionModel().
	//
	//		return selectedItem;
	//	}

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
		setOnKeyPressed(keyEvent -> Common.tryCatch(() -> {
			if (keyEvent.getCode() == KeyCode.ENTER)
			{
				MatrixItem currentItem = currentItem();
				if (currentItem != null)
				{
					GridPane layout = (GridPane) currentItem.getLayout();
					layout.getChildren().stream().filter(n -> n instanceof GridPane).findFirst().ifPresent(p -> Common.setFocused((TextField) ((GridPane) p).getChildren().get(0)));
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
		}, "Error on do actions by shortcuts"));

		setOnKeyReleased(keyEvent -> Common.tryCatch(() -> {
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
					box.setOnAction(event -> {
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

	private void expand(TreeItem<MatrixItem> rootItem, boolean flag)
	{
		rootItem.getChildren().forEach(item -> {
			item.setExpanded(flag);
			expand(item, flag);
		});
	}

	public void scrollTo(int index)
	{
		MatrixTreeViewSkin skin = (MatrixTreeViewSkin) getSkin();
		if (!skin.isIndexVisible(index))
		{
			skin.show(index);
		}
	}

	private class MatrixTreeViewSkin extends TreeTableViewSkin<MatrixItem>
	{

		public MatrixTreeViewSkin(TreeTableView<MatrixItem> treeView)
		{
			super(treeView);
		}

		public boolean isIndexVisible(int index)
		{
			return flow.getFirstVisibleCell() != null &&
					flow.getLastVisibleCell() != null &&
					flow.getFirstVisibleCell().getIndex() <= index - 1 &&
					flow.getLastVisibleCell().getIndex() >= index + 1;
		}

		public void show(int index)
		{
			flow.show(index);
		}
	}
}
