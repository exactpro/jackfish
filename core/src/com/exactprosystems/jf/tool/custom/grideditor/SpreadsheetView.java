////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.api.common.Sys;
import com.exactprosystems.jf.tool.helpers.DialogsHelper;
import javafx.application.Platform;
import javafx.beans.property.*;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.MenuItem;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

import java.awt.*;
import java.util.*;
import java.util.List;
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

	private static final int minProgression = Integer.MIN_VALUE;
	private static int currentIndexColumn = 0;
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
				Platform.runLater(future);
				future.get();
			}
			catch (InterruptedException | ExecutionException ex)
			{
				//
			}
		}
	}

	public final void display(DataProvider<String> dataProvider)
	{
		this.setDataProvider(dataProvider);
	}

	public void addColumn(int index)
	{
		this.providerProperty().get().addNewColumn(index);
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
		this.providerProperty.get().setColumnValues(columnIndex,
				IntStream.range(0, this.providerProperty.get().rowCount())
						.mapToObj(i -> on ? "" : "x")
						.toArray()
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

	public void extentionView(ObservableList<ObservableList<String>> initial, Direction direction, RectangleSelection.GridRange range)
	{
		Map<Point, String> map = new LinkedHashMap<>();
		switch (direction)
		{
			case UP:
				for (int j = range.getLeft(); j < range.getRight() + 1; j++)
				{
					ObservableList<String> strings = FXCollections.observableArrayList();
					int size = initial.size();
					for (ObservableList<String> s : initial)
					{
						strings.add(s.get(j - range.getLeft()));
					}
					Collections.reverse(strings);
					int progression = strings.size() == 1 ? 1 : getProgression(strings);
					int skip = progression != minProgression ? 1 : size;
					int currentProgression = progression != minProgression ? progression : -1;
					for (int i = range.getBottom() - size; i > range.getTop() - 1; i--)
					{
						String value = String.valueOf(this.providerProperty.get().getCellValue(j, i + skip));
						String evaluatedText = getEvaluatedText(value, currentProgression);
						map.put(new Point(j, i), evaluatedText);
					}
				}

				break;

			case DOWN:
				for (int j = range.getLeft(); j < range.getRight() + 1; j++)
				{
					ObservableList<String> strings = FXCollections.observableArrayList();
					int size = initial.size();
					for (ObservableList<String> s : initial)
					{
						strings.add(s.get(j - range.getLeft()));
					}
					int progression = strings.size() == 1 ? 1 : getProgression(strings);
					int skip = progression != minProgression ? 1 : size;
					int currentProgression = progression != minProgression ? progression : 1;
					String value = String.valueOf(this.providerProperty.get().getCellValue(j, range.getTop() + size - skip));
					for (int i = range.getTop() + size - skip; i < range.getTop() + size; i++)
					{
						String a = String.valueOf(this.providerProperty.get().getCellValue(j,i));
						if (a != null){
							value = a;
						}
						else
						{
							break;
						}

					}
					int newProgression = currentProgression;
					for (int i = range.getTop() + size; i < range.getBottom() + 1; i++)
					{
						if (i != (range.getTop() + size)){
							newProgression = newProgression + currentProgression;
						}
						String evaluatedText = getEvaluatedText(value, newProgression);
						map.put(new Point(j, i), evaluatedText);
					}
				}
				break;

			case LEFT:
				for (int i = 0; i < initial.size(); i++)
				{
					ObservableList<String> strings = initial.get(i);
					Collections.reverse(strings);
					int progression = strings.size() == 1 ? 1 : getProgression(strings);
					int skip = progression != minProgression ? 1 : strings.size();
					int currentProgression = progression != minProgression ? progression : -1;
					for (int j = range.getRight() - strings.size(); j > range.getLeft() - 1; j--)
					{
						String value = String.valueOf(this.providerProperty.get().getCellValue(j + skip, i + range.getTop()));
						String evaluatedText = getEvaluatedText(value, currentProgression);
						map.put(new Point(j, i + range.getTop()), evaluatedText);
					}
				}

				break;

			case RIGHT:
				for (int i = 0; i < initial.size(); i++)
				{
					ObservableList<String> strings = initial.get(i);
					int progression = strings.size() == 1 ? 1 : getProgression(strings);
					int skip = progression != minProgression ? 1 : strings.size();
					int currentProgression = progression != minProgression ? progression : 1;
					for (int j = range.getLeft() + strings.size(); j < range.getRight() + 1; j++)
					{
						String evaluatedText = getEvaluatedText(String.valueOf(this.providerProperty.get().getCellValue(j - skip, i + range.getTop())), currentProgression);
						map.put(new Point(j, i + range.getTop()), evaluatedText);
					}
				}
				break;
		}
		this.providerProperty.get().updateCells(map);
	}

	private static int getProgression(List<String> strings)
	{
		int progression = minProgression;
		ArrayList<Integer> list = new ArrayList<>();
		for (int i = 0; i < strings.size() - 1; i++)
		{
			String firstValue = strings.get(i);
			String secondValue = strings.get(i + 1);
			try
			{
				//TODO think about numberWORDnumber
				int firstInt = Integer.parseInt(firstValue);
				int secondInt = Integer.parseInt(secondValue);
				list.add(secondInt - firstInt);
			}
			catch (NumberFormatException e)
			{
				try
				{

					int firstInt = Integer.parseInt(getSubstring(firstValue));
					int secondInt = Integer.parseInt(getSubstring(secondValue));
					list.add(secondInt - firstInt);
				}
				catch (Exception ex){
					return progression;
				}
			}
		}
		if (list.stream().distinct().count() == 1)
		{
			progression = list.get(0);
		}
		return progression;
	}

	private static String getSubstring(String s)
	{
		String res = null;
		Pattern compile = Pattern.compile("^(-?\\d+)?(.*?)(-?\\d+)?$");
		Matcher matcher = compile.matcher(s);
		if (matcher.find())
		{
			res = matcher.group(3);
		}
		if (res != null)
		{
			return res;
		}
		else
		{
			return s;
		}
	}

	private String getEvaluatedText(String s, int progression)
	{
		/**
		 *  only number
		 */
		try
		{
			int i = Integer.parseInt(s);
			int w = i + progression;
			return String.valueOf(w);
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
			Pattern compile = Pattern.compile("^(-?\\d+)?(.*?)(-?\\d+)?$");
			Matcher matcher = compile.matcher(s);
			if (matcher.find())
			{
				String firstDigits = matcher.group(1);
				if (firstDigits != null)
				{
					int first = Integer.parseInt(firstDigits);
					int firstNew = first + progression;
					res.append(firstNew);
				}

				String word = matcher.group(2);
				res.append(word);

				String lastDigits = matcher.group(3);
				if (lastDigits != null)
				{
					int last = Integer.parseInt(lastDigits);
					int lastNew = last + progression;
					res.append(lastNew);
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
		return s;
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
		MenuItem addRowBefore = new MenuItem("Add before");
		addRowBefore.setOnAction(e -> addRowBefore(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem addRowAfter = new MenuItem("Add after");
		addRowAfter.setOnAction(e -> addRowAfter(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem removeRow = new MenuItem("Remove rows");
		removeRow.setOnAction(e -> removeRows(this.getSelectionModel().getSelectedCells().stream().map(TablePositionBase::getRow).distinct().collect(Collectors.toList())));

		MenuItem copyItems = new MenuItem("Copy");
		copyItems.setOnAction(event -> copy(false));

		MenuItem copyWithHeader = new MenuItem("Copy with header");
		copyWithHeader.setOnAction(event -> copy(true));

		MenuItem pasteItems = new MenuItem("Paste");
		pasteItems.setOnAction(event -> paste(false));

		MenuItem pasteWithHeader = new MenuItem("Paste with header");
		pasteWithHeader.setOnAction(event -> paste(true));
		contextMenu.getItems().addAll(addRowBefore, addRowAfter, removeRow, new SeparatorMenuItem(), copyItems, copyWithHeader, pasteItems, pasteWithHeader);
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
			DialogsHelper.showInfo("Can't paste to selected cells, cause selected cell is not one");
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
					getSelectionModel().clearAndSelectNextCell();
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
