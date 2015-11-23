////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.WeakSetChangeListener;
import javafx.event.EventHandler;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewFocusModel;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Optional;
import java.util.stream.IntStream;

public class CellView extends TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell>
{
	private final SpreadsheetHandle handle;

	private static final String ANCHOR_PROPERTY_KEY = "table.anchor"; //$NON-NLS-1$

	static TablePositionBase<?> getAnchor(Control table, TablePositionBase<?> focusedCell)
	{
		return hasAnchor(table) ? (TablePositionBase<?>) table.getProperties().get(ANCHOR_PROPERTY_KEY) : focusedCell;
	}

	static boolean hasAnchor(Control table)
	{
		return table.getProperties().get(ANCHOR_PROPERTY_KEY) != null;
	}

	static void setAnchor(Control table, TablePositionBase anchor)
	{
		if (anchor == null)
		{
			removeAnchor(table);
		}
		else
		{
			table.getProperties().put(ANCHOR_PROPERTY_KEY, anchor);
		}
	}

	static void removeAnchor(Control table)
	{
		table.getProperties().remove(ANCHOR_PROPERTY_KEY);
	}

	public CellView(SpreadsheetHandle handle)
	{
		this.handle = handle;
		EventHandler<MouseEvent> startFullDragEventHandler = mouseEvent -> {
			{
				if (this.handle.getCellsViewSkin().getSelectedColumns().size() == 1 && this.handle.getCellsViewSkin().getSelectedRows().size() == 1)
				{
					setAnchor(getTableView(), getTableView().getFocusModel().getFocusedCell());
				}
				if (this.handle.getGridView().getSelectionModel().getSelectionMode().equals(SelectionMode.MULTIPLE))
				{
					//setAnchor(getTableView(), getTableView().getFocusModel().getFocusedCell());
					startFullDrag();
				}
			}
		};
		this.addEventHandler(MouseEvent.DRAG_DETECTED, startFullDragEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
			if (event.getY() + 10 > getHeight() && event.getX() + 10 > getWidth() && this.isSelected())
			{
				this.setCursor(Cursor.CROSSHAIR);
			}
			else
			{
				this.setCursor(Cursor.DEFAULT);
			}
		});
		setOnMouseDragEntered(dragMouseEventHandler);
		setOnMouseDragReleased(event -> {
			CellView source = ((CellView) event.getGestureSource());
			if (source.getCursor() != null && source.getCursor().equals(Cursor.CROSSHAIR))
			{
				final RectangleSelection.GridRange range = this.handle.getCellsViewSkin().getRectangleSelection().getRange();
				final DataProvider provider = this.handle.getView().getProvider();
				String text = source.getText();
				IntStream.range(range.getLeft(), range.getRight()).forEach(i -> IntStream.range(range.getTop(), range.getBottom()).forEach(j -> provider.setCellValue(i, j, text)));
				this.handle.getView().setDataProvider(provider);
			}
		});
		itemProperty().addListener(itemChangeListener);
	}

	@Override
	public void startEdit()
	{
		if (!isEditable())
		{
			getTableView().edit(-1, null);
			return;
		}
		final SpreadsheetView spv = handle.getView();
		if (getTableRow() != null && !getTableRow().isManaged())
		{
			return;
		}
		GridCellEditor editor = getEditor(getItem(), spv);
		if (editor != null)
		{
			super.startEdit();
			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			editor.startEdit();
		}
		else
		{
			getTableView().edit(-1, null);
		}
	}

	@Override
	public void commitEdit(SpreadsheetCell newValue)
	{
		if (!isEditing())
		{
			return;
		}
		super.commitEdit(newValue);

		setContentDisplay(ContentDisplay.LEFT);
		updateItem(newValue, false);

		if (getTableView() != null)
		{
			getTableView().requestFocus();
		}
	}

	@Override
	public void cancelEdit()
	{
		if (!isEditing())
		{
			return;
		}
		super.cancelEdit();

		setContentDisplay(ContentDisplay.LEFT);
		updateItem(getItem(), false);

		if (getTableView() != null)
		{
			getTableView().requestFocus();
		}
	}

	@Override
	public void updateItem(final SpreadsheetCell item, boolean empty)
	{
		final boolean emptyRow = getTableView().getItems().size() < getIndex() + 1;
		if (!isEditing())
		{
			super.updateItem(item, empty && emptyRow);
		}
		if (empty && isSelected())
		{
			updateSelected(false);
		}
		if (empty && emptyRow)
		{
			textProperty().unbind();
			setText(null);
			setContentDisplay(null);
		}
		else if (!isEditing() && item != null)
		{
			show(item);
			setGraphic(null);
		}
	}

	public void show(final SpreadsheetCell cell)
	{
		textProperty().bind(cell.textProperty());
		setWrapText(cell.isWrapText());
		setEditable(cell.isEditable());
	}

	public void show()
	{
		if (getItem() != null)
		{
			show(getItem());
		}
	}

	private GridCellEditor getEditor(final SpreadsheetCell cell, final SpreadsheetView spv)
	{
		StringCellType cellType = cell.getCellType();
		Optional<SpreadsheetCellEditor> cellEditor = spv.getEditor(cellType);

		if (cellEditor.isPresent())
		{
			GridCellEditor editor = handle.getCellsViewSkin().getSpreadsheetCellEditorImpl();
			if (editor.isEditing())
			{
				editor.endEdit(false);
			}

			editor.updateSpreadsheetCell(this);
			editor.updateDataCell(cell);
			editor.updateSpreadsheetCellEditor(cellEditor.get());
			return editor;
		}
		else
		{
			return null;
		}
	}

	private final SetChangeListener<String> styleClassListener = arg0 -> {
		if (arg0.wasAdded())
		{
			getStyleClass().add(arg0.getElementAdded());
		}
		else if (arg0.wasRemoved())
		{
			getStyleClass().remove(arg0.getElementRemoved());
		}
	};

	private final WeakSetChangeListener<String> weakStyleClassListener = new WeakSetChangeListener<>(styleClassListener);

	private ChangeListener<String> styleListener;
	private WeakChangeListener<String> weakStyleListener;

	private void dragSelect(MouseEvent e)
	{
		if (!this.contains(e.getX(), e.getY()))
		{
			return;
		}
		final TableView<ObservableList<SpreadsheetCell>> tableView = getTableView();
		if (tableView == null)
		{
			return;
		}

		final int count = tableView.getItems().size();
		if (getIndex() >= count)
		{
			return;
		}

		final TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = tableView.getSelectionModel();
		if (sm == null)
		{
			return;
		}

		final int row = getIndex();
		final int column = tableView.getVisibleLeafIndex(getTableColumn());

		final SpreadsheetCell cell = getItem();
		final int rowCell = cell.getRow();
		final int columnCell = cell.getColumn();

		final TableViewFocusModel<?> fm = tableView.getFocusModel();
		if (fm == null)
		{
			return;
		}

		final TablePositionBase<?> focusedCell = fm.getFocusedCell();
		final MouseButton button = e.getButton();
		if (button == MouseButton.PRIMARY)
		{
			final TablePositionBase<?> anchor = getAnchor(tableView, focusedCell);
			int minRow = Math.min(anchor.getRow(), row);
			minRow = Math.min(minRow, rowCell);

			int maxRow = Math.max(anchor.getRow(), row);
			maxRow = Math.max(maxRow, rowCell);

			int minColumn = Math.min(anchor.getColumn(), column);
			minColumn = Math.min(minColumn, columnCell);

			int maxColumn = Math.max(anchor.getColumn(), column);
			maxColumn = Math.max(maxColumn, columnCell);

			if (!e.isShortcutDown())
				sm.clearSelection();
			if (minColumn != -1 && maxColumn != -1)
				sm.selectRange(minRow, tableView.getColumns().get(minColumn), maxRow, tableView.getColumns().get(maxColumn));
			setAnchor(tableView, anchor);
		}

	}

	public static void getValue(final Runnable runnable)
	{
		if (Platform.isFxApplicationThread())
		{
			runnable.run();
		}
		else
		{
			Platform.runLater(runnable);
		}
	}

	private final EventHandler<MouseEvent> dragMouseEventHandler = CellView.this::dragSelect;

	private final ChangeListener<SpreadsheetCell> itemChangeListener = ((observable, oldItem, newItem) -> {
		{
			if (oldItem != null)
			{
				oldItem.getStyleClass().removeListener(weakStyleClassListener);

				if (oldItem.styleProperty() != null)
				{
					oldItem.styleProperty().removeListener(weakStyleListener);
				}
			}
			if (newItem != null)
			{
				getStyleClass().clear();
				getStyleClass().setAll(newItem.getStyleClass());

				newItem.getStyleClass().addListener(weakStyleClassListener);

				if (newItem.styleProperty() != null)
				{
					initStyleListener();
					newItem.styleProperty().addListener(weakStyleListener);
					setStyle(newItem.getStyle());
				}
				else
				{
					//We clear the previous style.
					setStyle(null);
				}
			}
		}
	});

	private void initStyleListener()
	{
		if (styleListener == null)
		{
			styleListener = (ObservableValue<? extends String> observable, String oldValue, String newValue) -> styleProperty().set(newValue);
		}
		weakStyleListener = new WeakChangeListener<>(styleListener);
	}
}
