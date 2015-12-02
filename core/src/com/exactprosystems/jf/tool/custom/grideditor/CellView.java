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
import javafx.collections.FXCollections;
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

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class CellView extends TableCell<ObservableList<SpreadsheetCell>, SpreadsheetCell>
{
	private final SpreadsheetHandle handle;
	private ObservableList<TablePosition> selectedTablePositions;
	public static boolean isCrosshair = false;

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
					ObservableList<TablePosition> selectedCells = this.handle.getGridView().getSelectionModel().getSelectedCells();
					this.selectedTablePositions = FXCollections.observableArrayList(selectedCells);
					findMinPositionAndSetAnchor(selectedCells);
					startFullDrag();
				}
			}
		};
		this.addEventHandler(MouseEvent.DRAG_DETECTED, startFullDragEventHandler);
		this.addEventHandler(MouseEvent.MOUSE_MOVED, event -> {
			List<TablePosition> selectedCells = this.handle.getView().getSelectionModel().getSelectedCells();
			if (selectedCells.size() > 0)
			{
				int maxColumn = selectedCells.stream().mapToInt(TablePosition::getColumn).max().getAsInt();
				int maxRow = selectedCells.stream().mapToInt(TablePositionBase::getRow).max().getAsInt();
				SpreadsheetCell item = this.getItem();
				if (event.getY() + 10 > getHeight() && event.getX() + 10 > getWidth() && item.getRow() == maxRow && item.getColumn() == maxColumn)
				{
					this.setCursor(Cursor.CROSSHAIR);
					isCrosshair = true;
				}
				else
				{
					this.setCursor(Cursor.DEFAULT);
					isCrosshair = false;
				}
			}
		});
		setOnMouseDragEntered(dragMouseEventHandler);
		setOnMouseDragReleased(event -> {
			CellView source = ((CellView) event.getGestureSource());
			if (source.getCursor() != null && source.getCursor().equals(Cursor.CROSSHAIR))
			{
				ObservableList<TablePosition> initialCells = source.getSelectedTablePositions();
				final RectangleSelection.GridRange range = this.handle.getCellsViewSkin().getRectangleSelection().getRange();
				final DataProvider provider = this.handle.getView().getProvider();
				/**
				 * only one selected cell. easy
				 */
				if (initialCells.size() == 1)
				{
					StringBuilder text = new StringBuilder(source.getText());
					IntStream.range(range.getTop(), range.getBottom() + 1)
							.forEach(j -> IntStream.range(range.getLeft(), range.getRight() + 1)
									.forEach(i -> provider.setCellValue(i, j, getEvaluatedText(text))));
				}
				else
				{
					int progression = 1;
					int startRow = initialCells.get(0).getRow();
					ObservableList<ObservableList<String>> strings = FXCollections.observableArrayList();
					int index = 0;
					strings.add(FXCollections.observableArrayList());
					for (TablePosition cell : initialCells)
					{
						String cellValue = (String) provider.getCellValue(cell.getColumn(), cell.getRow());
						if (cell.getRow() != startRow)
						{
							ObservableList<String> list = FXCollections.observableArrayList();
							strings.add(list);
							list.add(cellValue);
							index++;
							startRow = cell.getRow();
						}
						else
						{
							strings.get(index).add(cellValue);
						}
					}

					int size = strings.size();
					for (int i = size; i < range.getBottom() - range.getTop() + 1; i++)
					{
						ObservableList<String> list = strings.get(i - size);
						ObservableList<String> collections = FXCollections.observableArrayList();
						for (String aList : list)
						{
							StringBuilder builder = new StringBuilder(aList);
							getEvaluatedText(builder, progression);
							collections.add(builder.toString());
						}
						strings.add(collections);
					}

					for (int i = range.getTop(); i < range.getBottom() + 1; i++)
					{
						int currentIndex = i - range.getTop();
						ObservableList<String> row = strings.get(currentIndex);
						List<StringBuilder> list = row.stream().map(StringBuilder::new).collect(Collectors.toList());
						for (int j = range.getLeft(); j < range.getRight() + 1; j++)
						{
							provider.setCellValue(j, i, getEvaluatedText(list.get((j - range.getLeft()) % row.size())));
						}
					}
				}
				this.handle.getView().setDataProvider(provider);

			}
		});
		itemProperty().addListener(itemChangeListener);
	}

	private void findMinPositionAndSetAnchor(ObservableList<TablePosition> selectedCells)
	{
		TablePosition min = selectedCells.get(0);
		for (TablePosition cell : selectedCells)
		{
			if (cell.getColumn() < min.getColumn() && cell.getRow() < min.getRow())
			{
				min = cell;
			}
		}
		setAnchor(getTableView(), min);
	}

	private int evaluateProgression(ObservableList<TablePosition> initialCells, DataProvider provider)
	{
		int progression = 1;
		int lastCell = 0;
		int firstCell = 0;
		ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < initialCells.size() - 1; i++)
		{
			TablePosition first = initialCells.get(i);
			TablePosition second = initialCells.get(i + 1);
			String firstValue = (String) provider.getCellValue(first.getColumn(), first.getRow());
			String secondValue = (String) provider.getCellValue(second.getColumn(), second.getRow());
			try
			{
				int firstInt = Integer.parseInt(firstValue);
				int secondInt = Integer.parseInt(secondValue);
				if (i == 0)
				{
					firstCell = firstInt;
				}
				if (i == initialCells.size() - 2)
				{
					lastCell = secondInt;
				}
				list.add(secondInt - firstInt);
			}
			catch (NumberFormatException e)
			{
				return progression;
			}
		}
		if (list.stream().distinct().count() == 1)
		{
			progression = list.get(0) + lastCell - firstCell;
		}
		return progression;
	}

	private String getEvaluatedText(StringBuilder sb, int progression)
	{
		String s = sb.toString();
		/**
		 *  only number
		 */
		try
		{
			int i = Integer.parseInt(s);
			sb.delete(0, sb.length());
			int w = i + progression;
			sb.append(w);
			return String.valueOf(i);
		}
		catch (Exception e)
		{
			//
		}
		/**
		 *  number add string
		 */
		try
		{
			StringBuilder res = new StringBuilder();
			Pattern compile = Pattern.compile("^(\\d+)?(.*?)(\\d+)?$");
			Matcher matcher = compile.matcher(sb.toString());
			if (matcher.find())
			{
				String firstDigits = matcher.group(1);
				int first = Integer.MIN_VALUE;
				int firstNew = Integer.MIN_VALUE;
				if (firstDigits != null)
				{
					first = Integer.parseInt(firstDigits);
					firstNew = first + progression;
				}

				String word = matcher.group(2);

				String lastDigits = matcher.group(3);
				int last = Integer.MIN_VALUE;
				int lastNew = Integer.MIN_VALUE;
				if (lastDigits != null)
				{
					last = Integer.parseInt(lastDigits);
					lastNew = last + progression;
				}

				if (first != Integer.MIN_VALUE)
				{
					res.append(first);
				}
				res.append(word);
				if (last != Integer.MIN_VALUE)
				{
					res.append(last);
				}
				sb.delete(0, sb.length());
				if (firstNew != Integer.MIN_VALUE)
				{
					sb.append(firstNew);
				}
				sb.append(word);
				if (lastNew != Integer.MIN_VALUE)
				{
					sb.append(lastNew);
				}
				return res.toString();
			}
		}
		catch (Exception e)
		{
			//
		}
		/**
		 *  only string
		 */
		return sb.toString();
	}

	private String getEvaluatedText(StringBuilder sb)
	{
		return getEvaluatedText(sb, 1);
	}

	public ObservableList<TablePosition> getSelectedTablePositions()
	{
		return selectedTablePositions;
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

		final TableViewFocusModel<ObservableList<SpreadsheetCell>> fm = tableView.getFocusModel();
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
