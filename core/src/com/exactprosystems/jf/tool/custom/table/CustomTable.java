////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.table;

import com.exactprosystems.jf.tool.CssVariables;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.ArrayList;
import java.util.List;

public class CustomTable<T> extends TableView<T>
{
	private CustomTableColumn firstColumn;
	private CustomTableColumn secondColumn;
	private CustomTableColumn thirdColumn;
	private boolean mayChanged;
	private ContextMenuListener<T> listener;

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

	public void update()
	{
		this.getColumns().forEach(column -> Platform.runLater(() -> {
			column.setVisible(false);
			column.setVisible(true);
		}));
//		final int length = getColumns().size();
//		this.getColumns().forEach(column -> Platform.runLater(() -> {
//			column.setMaxWidth(getWidth() / length);
//			column.setPrefWidth(getWidth() / length);
//		}));
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
		column.setCellFactory(tsTableColumn -> new CustomTableCell(mayChanged));
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

		public CustomTableCell(boolean mayChanged)
		{
			MenuItem removeSelected = new MenuItem("Remove selected");
			removeSelected.setGraphic(new ImageView(new Image(CssVariables.Icons.DELETE_ICON)));
			MenuItem removeAll = new MenuItem("Remove all");
			if (mayChanged)
			{
				ContextMenu contextMenu = new ContextMenu();
				contextMenu.getItems().addAll(removeSelected, removeAll);
				setContextMenu(contextMenu);
			}

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

			removeSelected.setOnAction(event -> deleteItems());
			removeAll.setOnAction(event -> deleteAllItems());
		}

		//TODO this 2 methods not right, because table is deleted their items yourself, is it not right, because this must do model
		//TODO Exactly!
		private void deleteItems()
		{
			List<T> selectedItems = FXCollections.observableArrayList(getTableView().getSelectionModel().getSelectedItems());
			List<T> list = new ArrayList<>();
			for (int i = selectedItems.size() - 1; i >= 0; i--)
			{
				T t = selectedItems.get(i);
				list.add(t);
				getTableView().getItems().remove(t);
			}
			onDeleteItems(list);
		}

		private void deleteAllItems()
		{
			getTableView().getItems().clear();
			onDeleteItems(getTableView().getItems());
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

	void onDeleteItems(List<T> items)
	{
		if (this.listener != null)
		{
			this.listener.onDeleteItems(items);
		}
	}
}
