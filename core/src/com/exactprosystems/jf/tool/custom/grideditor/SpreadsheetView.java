/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.tool.Common;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

import java.awt.Point;
import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpreadsheetView extends Control
{
	public enum Direction
	{
		UP,
		DOWN,
		LEFT,
		RIGHT
	}

	private static final double DEFAULT_ROW_HEADER_WIDTH = 30.0;

	private final SpreadsheetGridView cellsView;
	private SimpleObjectProperty<DataProvider> providerProperty = new SimpleObjectProperty<>();

	private final ObservableMap<Integer, Picker> rowPickers = FXCollections.observableHashMap();

	private final ObservableMap<Integer, Picker> columnPickers = FXCollections.observableHashMap();

	private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
	private Map<StringCellType, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
	private final SpreadsheetViewSelectionModel selectionModel;

	private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);

	private final BitSet columnWidthSet = new BitSet();

	final SpreadsheetHandle handle = new SpreadsheetHandle()
	{
		public SpreadsheetView getView()
		{
			return SpreadsheetView.this;
		}

		public GridViewSkin getCellsViewSkin()
		{
			return SpreadsheetView.this.getCellsViewSkin();
		}

		public SpreadsheetGridView getGridView()
		{
			return SpreadsheetView.this.getCellsView();
		}

		public boolean isColumnWidthSet(int indexColumn)
		{
			return columnWidthSet.get(indexColumn);
		}
	};

	final GridViewSkin getCellsViewSkin()
	{
		return (GridViewSkin) (cellsView.getSkin());
	}

	final SpreadsheetGridView getCellsView()
	{
		return cellsView;
	}

	void columnWidthSet(int indexColumn)
	{
		columnWidthSet.set(indexColumn);
	}

	public SpreadsheetView(DataProvider provider)
	{
		super();
		getStyleClass().add("SpreadsheetView"); //$NON-NLS-1$
		setSkin(new Skin<SpreadsheetView>()
		{
			@Override
			public Node getNode()
			{
				return SpreadsheetView.this.getCellsView();
			}

			@Override
			public SpreadsheetView getSkinnable()
			{
				return SpreadsheetView.this;
			}

			@Override
			public void dispose()
			{
				// no-op
			}
		});

		this.cellsView = new SpreadsheetGridView(handle);

		getChildren().add(cellsView);

		TableViewSpanSelectionModel tableViewSpanSelectionModel = new TableViewSpanSelectionModel(this, cellsView);
		cellsView.setSelectionModel(tableViewSpanSelectionModel);
		tableViewSpanSelectionModel.setCellSelectionEnabled(true);
		tableViewSpanSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		selectionModel = new SpreadsheetViewSelectionModel(this, tableViewSpanSelectionModel);
		cellsView.getFocusModel().focusedCellProperty().addListener((ChangeListener<TablePosition>) (ChangeListener<?>) new FocusModelListener(this, cellsView));

		cellsView.setOnKeyPressed(keyPressedHandler);

		setContextMenu(getSpreadsheetViewContextMenu());

		setDataProvider(provider);
		this.cellsView.setEditable(true);
	}

	public final void setDataProvider(DataProvider provider)
	{
		if (provider == null)
		{
			return;
		}
		providerProperty.set(provider);
		List<Double> widthColumns = columns.stream().map(SpreadsheetColumn::getWidth).collect(Collectors.toList());
		Pair<Integer, Integer> focusedPair = null;
		TablePosition focusedCell = cellsView.getFocusModel().getFocusedCell();
		if (focusedCell != null && focusedCell.getRow() != -1 && focusedCell.getColumn() != -1)
		{
			focusedPair = new Pair<>(focusedCell.getRow(), focusedCell.getColumn());
		}

		final Pair<Integer, Integer> finalPair = focusedPair;

		if (provider.columnCount() > 0 && provider.rowCount() > 0)
		{
			final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections.observableArrayList(provider.getRows());
			cellsView.getItems().clear();
			cellsView.setItems(observableRows);

			final int columnCount = provider.columnCount();
			columns.clear();
			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex)
			{
				final SpreadsheetColumn spreadsheetColumn = new SpreadsheetColumn(getTableColumn(provider, columnIndex), this, columnIndex, provider);
				if (widthColumns.size() > columnIndex)
				{
					spreadsheetColumn.setPrefWidth(widthColumns.get(columnIndex));
				}
				columns.add(spreadsheetColumn);
			}
		}
		List<Pair<Integer, Integer>> selectedCells = getSelectionModel().getSelectedCells().stream().map(position -> new Pair<>(position.getRow(), position.getColumn())).collect(Collectors.toList());

		Runnable runnable = () ->
		{
			if (cellsView.getColumns().size() > provider.columnCount())
			{
				cellsView.getColumns().remove(provider.columnCount(), cellsView.getColumns().size());
			}
			else if (cellsView.getColumns().size() < provider.columnCount())
			{
				for (int i = cellsView.getColumns().size(); i < provider.columnCount(); ++i)
				{
					cellsView.getColumns().add(columns.get(i).column);
				}
			}
			((TableViewSpanSelectionModel) cellsView.getSelectionModel()).verifySelectedCells(selectedCells);
			if (finalPair != null && finalPair.getKey() < getProvider().rowCount() && finalPair.getValue() < getProvider().columnCount())
			{
				cellsView.getFocusModel().focus(finalPair.getKey(), cellsView.getColumns().get(finalPair.getValue()));
			}

			removeAndAddListener();
		};

		if (Platform.isFxApplicationThread())
		{
			runnable.run();
		}
		else
		{
			try
			{
				FutureTask<Void> future = new FutureTask<>(runnable, null);
				Common.runLater(future);
				future.get();
			}
			catch (InterruptedException | ExecutionException ex)
			{
				//
			}
		}
	}

	private void removeAndAddListener()
	{
		if (this.oldListener != null)
		{
			cellsView.getColumns().removeListener(this.oldListener);
		}
		TableColumn[] columns = cellsView.getColumns().toArray(new TableColumn[cellsView.getColumns().size()]);
		this.oldListener = new ColumnChangeListener(columns);
		cellsView.getColumns().addListener(this.oldListener);
	}

	public final void display(DataProvider<String> dataProvider)
	{
		this.setDataProvider(dataProvider);
		removeAndAddListener();
	}

	private ColumnChangeListener oldListener;

	private class ColumnChangeListener implements ListChangeListener<TableColumn<ObservableList<SpreadsheetCell>, ?>>
	{
		boolean suspended;
		private TableColumn[] columns;

		public ColumnChangeListener(TableColumn[] columns)
		{
			this.columns = columns;
		}

		@Override
		public void onChanged(Change<? extends TableColumn<ObservableList<SpreadsheetCell>, ?>> change)
		{
			change.next();
			if (change.wasReplaced() && !suspended) {
				this.suspended = true;
				cellsView.getColumns().setAll(columns);
				this.suspended = false;
			}
		}
	}

	public void addColumn(int index)
	{
		this.providerProperty().get().addNewColumn(index);
	}

	public void swapColumns(int current, int swapTo)
	{
		this.providerProperty().get().swapColumns(current, swapTo);
		this.setDataProvider(this.providerProperty().get());
	}

	public void removeColumns(List<Integer> columns)
	{
		this.providerProperty().get().removeColumns(columns.toArray(new Integer[columns.size()]));
	}

	public void renameColumn(SpreadsheetColumn column, String text)
	{
		this.providerProperty().get().setColumnName(this.columns.indexOf(column), text);
	}

	public void switchColumn(boolean on, int columnIndex)
	{
		int range = this.providerProperty.get().rowCount();
		this.providerProperty.get().setColumnValues(columnIndex,
				IntStream.range(0, range)
						.mapToObj(i -> on ? "" : "x")
						.collect(Collectors.toList())
						.toArray(new String[range])
		);
	}

	public void addRowBefore(Integer row)
	{
		this.providerProperty().get().addRow(row);
		this.setDataProvider(this.providerProperty().get());
	}

	public void addRowAfter(Integer row)
	{
		this.providerProperty().get().addRow(row + 1);
		this.setDataProvider(this.providerProperty().get());
	}

	public void removeRows(List<Integer> rows)
	{
		this.providerProperty().get().removeRows(rows.toArray(new Integer[rows.size()]));
	}

	public void swapRows(int current, int swapTo)
	{
		this.providerProperty().get().swapRows(current, swapTo);
		this.setDataProvider(this.providerProperty().get());
	}

	public void extentionView(ObservableList<ObservableList<String>> initial, Direction direction, RectangleSelection.GridRange range)
	{
		List<String> strings;
		int count;
		List<String> values;
		List<List<String>> lists;
		Map<Point, String> map = new LinkedHashMap<>();

		switch (direction)
		{
			case UP:
				if (initial.size() == 1)
				{
					lists = new ArrayList<>((initial.get(0).stream().map(Arrays::asList).collect(Collectors.toList())));
				}else
				{
					Collections.reverse(initial);
					lists = new ArrayList<>(IntStream.range(0, initial.get(0).size())
							.mapToObj(i -> Arrays.asList(initial.get(0).get(i), initial.get(1).get(i)))
							.collect(Collectors.toList()));
				}

				for (int j = 0; j < lists.size(); j++)
				{
					strings = lists.get(j);
					count = range.getBottom() - range.getTop() - strings.size() +1;
					values = getNextValues(strings, count);
					for (int i = 0; i < values.size(); i++)
					{
						map.put(new Point(range.getLeft() + j, range.getBottom() - strings.size() - i), values.get(i));
					}
				}

				break;

			case DOWN:
				if (initial.size() == 1)
				{
					lists = new ArrayList<>(initial.get(0).stream().map(Arrays::asList).collect(Collectors.toList()));
				}else
				{
					lists = new ArrayList<>(IntStream.range(0, initial.get(0).size())
							.mapToObj(i -> Arrays.asList(initial.get(0).get(i), initial.get(1).get(i)))
							.collect(Collectors.toList()));
				}

				for (int j = 0; j < lists.size(); j++)
				{
					strings = lists.get(j);

					count = range.getBottom() - range.getTop() - strings.size() +1;
					values = getNextValues(strings, count);
					for (int i = 0; i < values.size(); i++)
					{
						map.put(new Point(range.getLeft() + j,range.getTop() + strings.size() + i), values.get(i));
					}
				}

				break;

			case LEFT:

				for (int j = 0; j < initial.size(); j++)
				{
					strings = initial.get(j);
					Collections.reverse(strings);
					count = range.getRight() - range.getLeft() - strings.size() + 1;
					values = getNextValues(strings, count);
					for (int i = 0; i < values.size(); i++)
					{
						map.put(new Point(range.getRight() - strings.size() - i, range.getTop() + j), values.get(i));
					}
				}

				break;

			case RIGHT:

				for (int j = 0; j < initial.size(); j++)
				{
					strings = initial.get(j);
					count = range.getRight() - range.getLeft() - strings.size() + 1;
					values = getNextValues(strings, count);
					for (int i = 0; i < values.size(); i++)
					{
						map.put(new Point(range.getLeft() + strings.size() + i, range.getTop() + j), values.get(i));
					}
				}

				break;
		}
		this.providerProperty.get().updateCells(map);
	}

	private List<String> getNextValues(List<String> list, int iter) {

		List<String> result = new ArrayList<>();

		switch (getKind(list))
		{
			case NUMBERS:
				List<Integer> collect = list.stream().map(Integer::parseInt).collect(Collectors.toList());
				List<Integer> values = IntStream.range(1, list.size())
						.mapToObj(i -> collect.get(i) - collect.get(i - 1))
						.collect(Collectors.toList());

				int progression = 0;
				if (values.stream().distinct().count() == 1) {
					progression = values.get(0);
				}

				if (progression != 0)
				{
					for (int i = 0; i < iter; i++)
					{
						int value = Integer.parseInt(list.get(list.size() - 1)) + progression * (1 + i);
						result.add(String.valueOf(value));
					}
					return result;
				}

			case STRINGS:
				Iterator<String> iterator = list.iterator();
				for (int i = 0; i < iter; i++)
				{
					if (!iterator.hasNext())
					{
						iterator = list.iterator();
					}
					result.add(iterator.next());
				}

				return result;

			case STRINGNUMBER:
				Pattern compile = Pattern.compile("^(.*?)(\\d+)$");

				List<String> strings1 = list.stream().map(s -> {
					Matcher matcher = compile.matcher(s);
					matcher.find();
					return matcher.group(1);
				}).collect(Collectors.toList());

				List<String> numbers = list.stream().map(s ->{
					Matcher matcher = compile.matcher(s);
					matcher.find();
					return matcher.group(2);
				}).collect(Collectors.toList());

				List<String> stringsFrom = getNextValues(strings1, iter);
				List<String> numbersFrom = getNextValues(numbers, iter);
				for (int i = 0; i < stringsFrom.size(); i++)
				{
					result.add(stringsFrom.get(i) + numbersFrom.get(i));
				}

				return result;
				default:
					return result;
		}
	}

	private StringKind getKind(List<String> list) {

		if (list.stream().allMatch(s -> s.matches("-?\\d+")))
		{
			return StringKind.NUMBERS;
		}
		if (list.stream().allMatch(s -> s.matches("^[a-zA-Zа-яА-Я0-9]*[a-zA-Zа-яА-Я]+\\d+")))
		{
			return StringKind.STRINGNUMBER;
		}

		return StringKind.STRINGS;
	}

	private enum StringKind
	{
		NUMBERS,
		STRINGS,
		STRINGNUMBER,
	}

	public ObservableList<String> convert(ObservableList<TablePosition> focusedCells)
	{
		return FXCollections.observableArrayList(focusedCells.stream().map(fc -> (String) this.providerProperty().get().getCellValue(fc.getColumn(), fc.getRow())).collect(Collectors.toList()));
	}

	public TablePosition<ObservableList<SpreadsheetCell>, ?> getEditingCell()
	{
		return cellsView.getEditingCell();
	}

	public final ObservableList<SpreadsheetColumn> getColumns()
	{
		return columns;
	}

	public final DataProvider getProvider()
	{
		return this.providerProperty.get();
	}

	public final ReadOnlyObjectProperty<DataProvider> providerProperty()
	{
		return providerProperty;
	}

	public final DoubleProperty rowHeaderWidthProperty()
	{
		return rowHeaderWidth;
	}

	public final double getRowHeaderWidth()
	{
		return rowHeaderWidth.get();
	}

	public ObservableMap<Integer, Picker> getRowPickers()
	{
		return rowPickers;
	}

	public ObservableMap<Integer, Picker> getColumnPickers()
	{
		return columnPickers;
	}

	public double getRowHeight(int row)
	{
		if (getCellsViewSkin() == null)
		{
			return -1;
		}
		else
		{
			return getCellsViewSkin().getRowHeight(row);
		}
	}

	public SpreadsheetViewSelectionModel getSelectionModel()
	{
		return selectionModel;
	}

	public final Optional<SpreadsheetCellEditor> getEditor(StringCellType cellType)
	{
		if (cellType == null)
		{
			return Optional.empty();
		}
		SpreadsheetCellEditor cellEditor = editors.get(cellType);
		if (cellEditor == null)
		{
			cellEditor = cellType.createEditor(this);
			if (cellEditor == null)
			{
				return Optional.empty();
			}
			editors.put(cellType, cellEditor);
		}
		return Optional.of(cellEditor);
	}

	public ContextMenu getSpreadsheetViewContextMenu()
	{
		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);
		MenuItem addRowBefore = new MenuItem(R.SSW_ADD_BEFORE.get());
		addRowBefore.setOnAction(e -> addRowBefore(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem addRowAfter = new MenuItem(R.SSW_ADD_AFTER.get());
		addRowAfter.setOnAction(e -> addRowAfter(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem moveUpRow = new MenuItem(R.SSW_MOVE_UP.get());
		moveUpRow.setOnAction(e ->
				{
					int currentRow = this.getSelectionModel().getSelectedCells().get(0).getRow();
					swapRows(currentRow, currentRow - 1);
				}
		);

		MenuItem moveDownRow = new MenuItem(R.SSW_MOVE_DOWN.get());
		moveDownRow.setOnAction(e ->
				{
					int currentRow = this.getSelectionModel().getSelectedCells().get(0).getRow();
					swapRows(currentRow, currentRow + 1);
				}
		);

		MenuItem removeRow = new MenuItem(R.SSW_REMOVE.get());
		removeRow.setOnAction(e -> removeRows(this.getSelectionModel().getSelectedCells().stream().map(TablePositionBase::getRow).distinct().collect(Collectors.toList())));

		MenuItem copyItems = new MenuItem(R.SSW_COPY.get());
		copyItems.setOnAction(event -> copy(false));

		MenuItem copyWithHeader = new MenuItem(R.SSW_COPY_WITH_HEADER.get());
		copyWithHeader.setOnAction(event -> copy(true));

		MenuItem pasteItems = new MenuItem(R.SSW_PASTE.get());
		pasteItems.setOnAction(event -> paste(false));

		MenuItem pasteWithHeader = new MenuItem(R.SSW_PASTE_WITH_HEADER.get());
		pasteWithHeader.setOnAction(event -> paste(true));
		contextMenu.getItems().addAll(addRowBefore, addRowAfter, removeRow, moveUpRow, moveDownRow, new SeparatorMenuItem(), copyItems, copyWithHeader, pasteItems, pasteWithHeader);
		return contextMenu;
	}

	private void copy(boolean withHeader)
	{
		StringBuilder copyString = new StringBuilder();

		List<TablePosition> selectedCells = this.getSelectionModel().getSelectedCells();
		List<ObservableList<SpreadsheetCell>> rows = this.providerProperty.get().getRows();
		if (withHeader)
		{
			int firstRow = selectedCells.get(0).getRow();
			String collect = selectedCells.stream().filter(cell -> firstRow == cell.getRow()).map(cell -> cell.getTableColumn().getText()).collect(Collectors.joining("\t"));
			copyString.append(collect).append("\n");
		}
		int temp = selectedCells.get(0).getRow();
		for (TablePosition cell : selectedCells)
		{
			int row = cell.getRow();
			int column = cell.getColumn();
			if (temp != row)
			{
				copyString.append("\n");
				temp = row;
			}
			copyString.append(rows.get(row).get(column).getText());
			copyString.append("\t");
		}
		Sys.copyToClipboard(copyString.toString());
	}

	private void paste(boolean withHeader)
	{
		List<TablePosition> selectedCells = this.getSelectionModel().getSelectedCells();
		if (selectedCells.size() != 1)
		{
			DialogsHelper.showInfo(R.SSW_PASTE_ERROR.get());
			return;
		}
		TablePosition selectedCell = selectedCells.get(0);
		this.providerProperty.get().paste(selectedCell.getColumn(), selectedCell.getRow(), withHeader);
	}

	public void deleteSelectedCells()
	{
		this.providerProperty.get().clearCells(getSelectionModel().getSelectedCells()
				.stream()
				.map(pos -> new Point(pos.getColumn(), pos.getRow()))
				.collect(Collectors.toList())
		);
	}

	private TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> getTableColumn(DataProvider provider, int columnIndex)
	{

		TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;

		String columnHeader = provider.getColumnHeaders().size() > columnIndex ? String.valueOf(provider.getColumnHeaders().get(columnIndex)) : "new";

		if (columnIndex < cellsView.getColumns().size())
		{
			column = (TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>) cellsView.getColumns().get(columnIndex);
			column.setText(columnHeader);
		}
		else
		{
			column = new TableColumn<>(columnHeader);

			column.widthProperty().addListener(observable -> this.getCellsViewSkin().resizeRowsToFitContent());
			column.setEditable(true);
			column.setSortable(false);
			column.setCellValueFactory((TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) ->
			{
				if (columnIndex >= p.getValue().size())
				{
					return null;
				}
				return new ReadOnlyObjectWrapper<>(p.getValue().get(columnIndex));
			});
			column.setCellFactory((TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> new CellView(handle));
		}
		return column;
	}

	private final EventHandler<KeyEvent> keyPressedHandler = (KeyEvent keyEvent) ->
	{
		TablePosition<ObservableList<SpreadsheetCell>, ?> position = getSelectionModel().getFocusedCell();
		// Go to the next row only if we're not editing
		if (getEditingCell() != null && keyEvent.getCode().equals(KeyCode.ESCAPE))
		{
			getSelectionModel().clearSelection();
		}
		else if (getEditingCell() == null && KeyCode.ENTER.equals(keyEvent.getCode()))
		{
			if (position != null)
			{
				if (keyEvent.isShiftDown())
				{
					getSelectionModel().clearAndSelectPreviousCell();
				}
				else
				{
					getSelectionModel().clearAndSelectNextCell(position.getColumn(), position.getRow());
				}
				keyEvent.consume();
			}
			getCellsViewSkin().scrollHorizontally();
		}
		else if (getEditingCell() == null && KeyCode.TAB.equals(keyEvent.getCode()))
		{
			if (position != null)
			{
				if (keyEvent.isShiftDown())
				{
					getSelectionModel().clearAndSelectLeftCell();
				}
				else
				{
					getSelectionModel().clearAndSelectRightCell();
				}
			}
			keyEvent.consume();
			getCellsViewSkin().scrollHorizontally();
		}
		else if (KeyCode.DELETE.equals(keyEvent.getCode()))
		{
			deleteSelectedCells();
		}
		else if (keyEvent.getCode() != KeyCode.SHIFT && !keyEvent.isShortcutDown() && !keyEvent.getCode().isNavigationKey() && keyEvent.getCode() != KeyCode.ESCAPE && !SpreadsheetColumn.isColumnEditable)
		{
			getCellsView().edit(position.getRow(), position.getTableColumn());
		}
	};

	public static class RowHeightEvent extends Event
	{

		public static final EventType<RowHeightEvent> ROW_HEIGHT_CHANGE = new EventType<>(Event.ANY, "RowHeightChange"); //$NON-NLS-1$

		private final int row;
		private final double height;

		public RowHeightEvent(int row, double height)
		{
			super(ROW_HEIGHT_CHANGE);
			this.row = row;
			this.height = height;
		}

		public int getRow()
		{
			return row;
		}

		public double getHeight()
		{
			return height;
		}
	}

	public static class ColumnWidthEvent extends Event
	{

		public static final EventType<ColumnWidthEvent> COLUMN_WIDTH_CHANGE = new EventType<>(Event.ANY, "ColumnWidthChange"); //$NON-NLS-1$

		private final int column;
		private final double width;

		public ColumnWidthEvent(int column, double width)
		{
			super(COLUMN_WIDTH_CHANGE);
			this.column = column;
			this.width = width;
		}

		public int getColumn()
		{
			return column;
		}

		public double getWidth()
		{
			return width;
		}
	}
}
