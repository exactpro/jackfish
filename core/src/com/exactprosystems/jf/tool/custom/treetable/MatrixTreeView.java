////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.treetable;

import com.exactprosystems.jf.common.Settings;
import com.exactprosystems.jf.documents.matrix.parser.items.End;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItemState;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
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
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.exactprosystems.jf.tool.Common.tryCatch;

public class MatrixTreeView extends TreeTableView<MatrixItem>
{
	private MatrixFx matrix;

	private final static Logger logger = Logger.getLogger(MatrixTreeView.class);

	private boolean isTracing;
	private boolean needExpand;

	public MatrixTreeView()
	{
		super(null);
		this.setSkin(new MatrixTreeViewSkin(this));
		this.setShowRoot(false);
		this.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
		this.getStyleClass().addAll(CssVariables.EMPTY_HEADER_COLUMN, CssVariables.CUSTOM_TREE_TABLE_VIEW);
		initTable();
	}

	public void setNeedExpand(boolean flag)
	{
		this.needExpand = flag;
	}

	public void init(MatrixFx matrix, Settings settings, ContextMenu contextMenu)
	{
		this.matrix = matrix;

		setRoot(new TreeItem<>(matrix.getRoot()));

		setRowFactory(treeView -> {
			MatrixTreeRow row = new MatrixTreeRow(contextMenu);
			shortCuts(row, settings);
			return row;
		});
	}

	public void setCurrent(TreeItem<MatrixItem> treeItem, boolean needExpand)
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
				if (needExpand)
				{
					expand(treeItem, !this.needExpand);
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
		Platform.runLater(() -> Optional.ofNullable(this.getColumns().get(0)).ifPresent(col ->
		{
			col.setVisible(false);
			col.setVisible(true);
		}));
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

    public void expand(TreeItem<MatrixItem> rootItem, boolean flag)
    {
		if (rootItem == null)
		{
			return;
		}
		rootItem.setExpanded(flag);
		rootItem.getChildren().forEach(item ->
		{
			item.setExpanded(flag);
			expand(item, flag);
		});
	}
	
	public List<MatrixItem> currentItems()
	{
		return getSelectionModel().getSelectedCells().stream().map(TreeTablePosition::getTreeItem).map(TreeItem::getValue).collect(Collectors.toList());
	}

	public MatrixItem currentItem()
	{
		TreeItem<MatrixItem> selectedItem = getSelectionModel().getSelectedItem();
		return selectedItem != null ? selectedItem.getValue() : null;
	}

	public void setTracing(boolean flag)
	{
		this.isTracing = flag;
	}

	public boolean isTracing()
	{
		return isTracing;
	}

	public TreeItem<MatrixItem> find(MatrixItem item)
	{
		return find(this.getRoot(), item);
	}

	public TreeItem<MatrixItem> find(TreeItem<MatrixItem> parent, MatrixItem item)
	{
		return find(parent, matrixItem -> Objects.equals(item, matrixItem));
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
					layout.getChildren().stream().filter(n -> n instanceof GridPane).findFirst().ifPresent(p -> Common.setFocused(((GridPane) p).getChildren().get(0)));
				}
			}
			else if (SettingsPanel.match(settings, keyEvent, Settings.SHOW_ALL))
			{
				row.showExpressionsResults();
			}
			else if (keyEvent.getCode() == KeyCode.ESCAPE)
			{
				MatrixTreeView.this.requestFocus();
			}
		}, "Error on do actions by shortcuts"));

		setOnKeyReleased(keyEvent -> Common.tryCatch(() -> {
			if (SettingsPanel.match(settings, keyEvent, Settings.SHOW_ALL))
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
		numberColumn.setCellFactory(p -> new TreeTableCell<MatrixItem, Integer>(){
			@Override
			protected void updateItem(Integer item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && item != -1 && !empty)
				{
					setText("" + item);
				}
				else
				{
					setText(null);
				}
			}
		});

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

		TreeTableColumn<MatrixItem, MatrixItem> reportOffColumn = new TreeTableColumn<>();
		reportOffColumn.setSortable(false);
		reportOffColumn.setMinWidth(25);
		reportOffColumn.setMaxWidth(26);
		reportOffColumn.setPrefWidth(25);
		reportOffColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));
		reportOffColumn.setCellFactory(p -> new TreeTableCell<MatrixItem, MatrixItem>()
		{
			private CheckBox box = new CheckBox();

			private void updateTooltip()
			{
				this.box.setTooltip(new Tooltip("Set report " + (this.box.isSelected() ? "on" : "off") + " item"));
			}

			@Override
			protected void updateItem(MatrixItem item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !(item instanceof End))
				{
					box.setSelected(item.isRepOff());
					updateTooltip();
					box.setOnAction(event -> {
						item.setRepOff(box.isSelected());
	                    matrix.changed(true);
						updateTooltip();
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

		TreeTableColumn<MatrixItem, MatrixItem> offColumn = new TreeTableColumn<>();
		offColumn.setSortable(false);
		offColumn.setMinWidth(25);
		offColumn.setMaxWidth(26);
		offColumn.setPrefWidth(25);
		offColumn.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getValue()));
		offColumn.setCellFactory(p -> new TreeTableCell<MatrixItem, MatrixItem>()
		{
			private CheckBox box = new CheckBox();

			private void updateTooltip()
			{
				this.box.setTooltip(new Tooltip("Set item " + (this.box.isSelected() ? "on" : "off")));
			}

			@Override
			protected void updateItem(MatrixItem item, boolean empty)
			{
				super.updateItem(item, empty);
				if (item != null && !(item instanceof End))
				{
					box.setSelected(item.isOff());
					updateTooltip();
					box.setOnAction(event -> {
						item.setOff(box.isSelected());
	                    matrix.changed(true); 
						updateTooltip();
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
		this.getColumns().add(reportOffColumn);
		this.getColumns().add(offColumn);
		this.getColumns().add(iconColumn);
		this.getColumns().add(gridColumn);
		gridColumn.setMaxWidth(Double.MAX_VALUE);
		gridColumn.prefWidthProperty().bind(this.widthProperty().subtract(numberColumn.getWidth() + iconColumn.getWidth() + offColumn.getWidth() + reportOffColumn.getWidth()).subtract(2));
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
			Platform.runLater(() -> flow.show(index));
		}
	}
}
