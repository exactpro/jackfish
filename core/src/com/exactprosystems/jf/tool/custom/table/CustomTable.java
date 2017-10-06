////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.table;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.CssVariables;
import javafx.collections.FXCollections;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

public class CustomTable<T> extends TableView<T>
{
	private CustomTableColumn firstColumn;
	private CustomTableColumn secondColumn;
	private CustomTableColumn thirdColumn;
	private CustomTableColumn fourthColumn;
	private boolean mayChanged;
	private Consumer<List<T>> listener;
	private ContextMenu contextMenu;

	public enum EditState
    {
        LABEL,
        TEXTFIELD,
        TEXTFIELD_READONLY
    }

	public CustomTable(boolean isChanged)
	{
		this(null);
		this.mayChanged = isChanged;
		
        getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.DELETE && keyEvent.isShiftDown())
            {
                deleteAllItems();
            }
            else if (keyEvent.getCode() == KeyCode.DELETE)
            {
                deleteItems();
            }
        });

	}

	public CustomTable(Consumer<List<T>> listener)
	{
		super();
		this.setColumnResizePolicy(CONSTRAINED_RESIZE_POLICY);
		this.firstColumn 		= new CustomTableColumn();
		this.secondColumn		= new CustomTableColumn();
		this.thirdColumn		= new CustomTableColumn();
		this.fourthColumn		= new CustomTableColumn();
		this.listener = listener;
		this.mayChanged = true;
		this.setEditable(true);
		this.contextMenu = new ContextMenu();
		this.setContextMenu(this.contextMenu);
		addItemsToContextMenu();
	}

	public void setListener(Consumer<List<T>> listener)
	{
		this.listener = listener;
	}

	public void completeFirstColumn(String name, String valueFactory, EditState editState, boolean needTooltip)
	{
		completeColumn(firstColumn, name, valueFactory, editState, needTooltip);
		showColumn(firstColumn, 0);
	}

	public void completeSecondColumn(String name, String valueFactory, EditState editState, boolean needTooltip)
	{
		completeColumn(secondColumn, name, valueFactory, editState, needTooltip);
		showColumn(secondColumn, 1);
	}

	public void completeThirdColumn(String name, String valueFactory, EditState editState, boolean needTooltip)
	{
		completeColumn(thirdColumn, name, valueFactory, editState, needTooltip);
		showColumn(thirdColumn, 2);
	}

	public void completeFourthColumn(String name, String valueFactory, EditState editState, boolean needTooltip)
	{
		completeColumn(fourthColumn, name, valueFactory, editState, needTooltip);
		showColumn(fourthColumn, 3);
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

	public void onFinishEditFourthColumn(EditCommit<T> editCommit)
	{
		onEditCommitColumn(this.fourthColumn, editCommit);
	}

	public void setSortable(boolean flag)
	{
		this.getColumns().forEach(col -> col.setSortable(flag));
	}

	public void update()
	{
		this.getColumns().forEach(column -> Common.runLater(() -> {
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
		onDeleteItems(selectedItems);
		this.getItems().removeAll(selectedItems);
	}

	private void deleteAllItems()
	{
		onDeleteItems(this.getItems());
		this.getItems().clear();
	}

    private void onEditCommitColumn(CustomTableColumn c, final EditCommit<T> editCommit)
    {
        c.setOnEditCommit(t ->
        {
            if (t.getTablePosition() != null)
            {
                T t1 = t.getRowValue();
                editCommit.onFinishEdit(t1, t.getNewValue());
            }
            update();
        });
    }

	private void completeColumn(CustomTableColumn column, String name, String valueFactory, EditState editState, boolean needTooltip)
	{
		column.setText(name);
		column.setCellValueFactory(new PropertyValueFactory<>(valueFactory));
		column.setNeedTooltip(needTooltip);
		column.setCellFactory(tsTableColumn -> new CustomTableCell());
		switch (editState)
        {
            case LABEL:
            {
                column.setEditable(false);
                break;
            }
            case TEXTFIELD:
            {
                column.setEditable(true);
                break;
            }
            case TEXTFIELD_READONLY:
            {
                column.setEditable(true);
                column.setReadOnly();
                break;
            }
        }
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

        CustomTableCell()
		{
		    super();
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
				Common.runLater(textField::requestFocus);
			}
		}

		@Override
		public void cancelEdit()
		{
			super.cancelEdit();
			setText(Str.asString(getItem()));
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
                setText(getString());
                setContentDisplay(ContentDisplay.TEXT_ONLY);

                CustomTableColumn column = ((CustomTableColumn) this.getTableColumn());
				if (column.isNeedTooltip())
				{
					Tooltip tip = new Tooltip(getString());
					Tooltip.install(this, tip);
				}
			}
		}

        @Override
        public void commitEdit(String newValue)
        {
            super.commitEdit(((CustomTableColumn) getTableColumn()).isReadOnly() ? getString() : newValue);
        }

		private String getString()
		{
			return Str.asString(getItem());
		}

		private void createTextField()
		{
			textField = new TextField(getString());
			textField.getStyleClass().add(CssVariables.TEXT_FIELD_VARIABLES);
			textField.setMinWidth(this.getWidth() - this.getGraphicTextGap() * 2);
			textField.setOnKeyPressed(t -> 
			{
				if (t.getCode() == KeyCode.ENTER || t.getCode() == KeyCode.TAB)
				{
					commitEdit(textField.getText());
				}
				else if (t.getCode() == KeyCode.ESCAPE)
				{
					cancelEdit();
				}
			});
			textField.focusedProperty().addListener((observable, oldValue, newValue) -> 
			{
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
        private boolean readOnly;

		CustomTableColumn()
		{
			super();
		}

		boolean isNeedTooltip()
		{
			return needTooltip;
		}

		void setNeedTooltip(boolean needTooltip)
		{
			this.needTooltip = needTooltip;
		}

        void setReadOnly()
        {
            this.readOnly = true;
        }

        boolean isReadOnly()
        {
            return this.readOnly;
        }
	}

	private void onDeleteItems(List<T> items)
	{
		Optional.ofNullable(this.listener).ifPresent(l -> l.accept(items));
	}
}
