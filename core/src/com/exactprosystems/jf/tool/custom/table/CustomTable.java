////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.table;

import com.exactprosystems.jf.tool.CssVariables;
import com.exactprosystems.jf.tool.newconfig.TablePair;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.event.Event;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CustomTable<T> extends TableView<T>
{
	private CustomTableColumn firstColumn;
	private CustomTableColumn secondColumn;
	private CustomTableColumn thirdColumn;
	private boolean mayChanged;
	private ContextMenuListener<T> listener;
	private ContextMenu contextMenu;

	public CustomTable(boolean isChanged)
	{
		this(null);
		this.mayChanged = isChanged;
	}

	public CustomTable(ContextMenuListener<T> listener)
	{
		super();
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
		this.firstColumn 		= new CustomTableColumn();
		this.secondColumn		= new CustomTableColumn();
		this.thirdColumn		= new CustomTableColumn();
		this.listener = listener;
		this.mayChanged = true;
		this.setEditable(true);
		this.contextMenu = new ContextMenu();
		this.setContextMenu(this.contextMenu);
		addItemsToContextMenu();
	}

	public void setListener(ContextMenuListener<T> listener)
	{
		this.listener = listener;
	}

	public void completeFirstColumn(String name, String valueFactory, boolean editable, boolean needTooltip)
	{
		completeColumn(firstColumn, name, valueFactory, editable, needTooltip);
		showColumn(firstColumn, 0);
	}

	public void completeSecondColumn(String name, String valueFactory, boolean editable, boolean needTooltip)
	{
		completeColumn(secondColumn, name, valueFactory, editable, needTooltip);
		showColumn(secondColumn, 1);
	}

	public void completeThirdColumn(String name, String valueFactory, boolean editable, boolean needTooltip)
	{
		completeColumn(thirdColumn, name, valueFactory, editable, needTooltip);
		showColumn(thirdColumn, 2);
	}

	public void onFinishEditFirstColumn(EditCommit<T> editCommit)
	{
		onEditCommitColumn(firstColumn, editCommit);
	}

	public void onFinishEditSecondColumn(EditCommit<T> editCommit)
	{
		onEditCommitColumn(secondColumn, editCommit);
	}

	public void onFinishEditThirdColumn(EditCommit<T> editCommit)
	{
		onEditCommitColumn(thirdColumn, editCommit);
	}

	public void setSortable(boolean flag)
	{
		this.getColumns().forEach(col -> col.setSortable(flag));
	}

	public void update()
	{
		this.getColumns().forEach(column -> Platform.runLater(() -> {
			column.setVisible(false);
			column.setVisible(true);
		}));
	}

	private void addItemsToContextMenu()
	{
		MenuItem removeSelected = new MenuItem("Remove selected");
		removeSelected.setGraphic(new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
		MenuItem removeAll = new MenuItem("Remove all");
		if (this.mayChanged)
		{
			ContextMenu contextMenu = new ContextMenu();
			contextMenu.getItems().addAll(removeSelected, removeAll);
			setContextMenu(contextMenu);
		}

		removeSelected.setOnAction(event -> deleteItems());
		removeAll.setOnAction(event -> deleteAllItems());
	}

	private void deleteItems()
	{
		List<T> selectedItems = FXCollections.observableArrayList(this.getSelectionModel().getSelectedItems());
		List<T> list = new ArrayList<>();
		for (int i = selectedItems.size() - 1; i >= 0; i--)
		{
			T t = selectedItems.get(i);
			list.add(t);
			this.getItems().remove(t);
		}
		onDeleteItems(list);
	}

	private void deleteAllItems()
	{
		onDeleteItems(this.getItems());
		this.getItems().clear();
	}

	private void onEditCommitColumn(CustomTableColumn c, final EditCommit<T> editCommit)
	{
		c.setOnEditCommit(t -> {
			T t1 = t.getTableView().getItems().get(t.getTablePosition().getRow());
			editCommit.onFinishEdit(t1, t.getNewValue());
			this.getColumns().forEach(column -> Platform.runLater(() -> {
				column.setVisible(false);
				column.setVisible(true);
			}));
		});
	}

	private void completeColumn(CustomTableColumn column, String name, String valueFactory, boolean editable, boolean needTooltip)
	{
		column.setText(name);
		column.setCellValueFactory(new PropertyValueFactory<>(valueFactory));
		column.setEditable(editable);
		column.setNeedTooltip(needTooltip);
		column.setCellFactory(tsTableColumn -> new CustomTableCell());
	}

	private void showColumn(CustomTableColumn c, int index)
	{
		this.getColumns().add(index, c);
		this.getColumns().forEach(column -> {
			if (column.prefWidthProperty().isBound())
			{
				column.prefWidthProperty().unbind();
			}
			column.prefWidthProperty().bind(this.widthProperty().divide(this.getColumns().size() == 0 ? 1 : this.getColumns().size()));
		});
	}

	private class CustomTableCell extends TableCell<T, String>
	{
		private TextField textField;

		public CustomTableCell()
		{
			Platform.runLater(() -> {
				getTableView().getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
				getTableView().setOnKeyPressed(keyEvent -> {
					if (keyEvent.getCode() == KeyCode.DELETE && keyEvent.isShiftDown())
					{
						deleteAllItems();
					}
					else if (keyEvent.getCode() == KeyCode.DELETE)
					{
						deleteItems();
					}
				});
			});
		}

		@Override
		public void startEdit()
		{
			super.startEdit();
			CustomTableColumn column = ((CustomTableColumn) getTableColumn());
			if (column.isEditable())
			{
				if (textField == null)
				{
					createTextField();
				}
				textField.setText(getString());
				setGraphic(textField);
				setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
				Platform.runLater(textField::requestFocus);
			}
		}

		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setText(String.valueOf(getItem()));
			setContentDisplay(ContentDisplay.TEXT_ONLY);
		}

		@Override
		protected void updateItem(String s, boolean b)
		{
			super.updateItem(s, b);
			if (b || s == null)
			{
				setText(null);
				setGraphic(null);
			}
			else
			{
				CustomTableColumn column = ((CustomTableColumn) this.getTableColumn());
				if (column.isNeedTooltip())
				{
					Tooltip tip = new Tooltip(getString());
					Tooltip.install(this, tip);
				}
//				if (isEditing())
//				{
//					if (textField != null)
//					{
//						textField.setText(getString());
//					}
//					setGraphic(textField);
//					setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
//				}
//				else
				{
					setText(getString());
					setContentDisplay(ContentDisplay.TEXT_ONLY);
				}
			}
		}

		@Override
		public void commitEdit(String item)
		{
			if (!isEditing() && !item.equals(getItem()))
			{
				TableView<T> table = getTableView();
				if (table != null)
				{
					TableColumn<T, String> column = getTableColumn();
					TableColumn.CellEditEvent<T, String> event = new TableColumn.CellEditEvent<>(table, new TablePosition<>(table, getIndex(), column), TableColumn.editCommitEvent(), item);
					Event.fireEvent(column, event);
				}
			}
		}

		private String getString()
		{
			return String.valueOf(getItem() == null ? "" : getItem());
		}

		private void createTextField()
		{
			textField = new TextField(getString());
			textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(t -> {
				if (t.getCode() == KeyCode.ENTER)
				{
					commitEdit(textField.getText());
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
				else if (t.getCode() == KeyCode.TAB)
				{
					commitEdit(textField.getText());
				}
			});
			textField.focusedProperty().addListener((observable, oldValue, newValue) -> {
				if (!newValue && textField != null)
				{
					commitEdit(textField.getText());
				}
			});
		}
	}

	private class CustomTableColumn extends TableColumn<T, String>
	{
		private boolean needTooltip;

		public CustomTableColumn()
		{
			super();
		}

		public boolean isNeedTooltip()
		{
			return needTooltip;
		}

		public void setNeedTooltip(boolean needTooltip)
		{
			this.needTooltip = needTooltip;
		}
	}

	private void onDeleteItems(List<T> items)
	{
		Optional.ofNullable(this.listener).ifPresent(l -> l.onDeleteItems(items));
	}
}
