////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.exceptions.ColumnIsPresentException;
import com.exactprosystems.jf.tool.Common;
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
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class SpreadsheetView extends Control
{

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

		Runnable runnable = () -> {
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

	public void addColumn(int index)
	{
		boolean flag = true;
		while (flag)
		{
			try
			{
				currentIndexColumn++;
				this.providerProperty().get().addColumn(index, "NewColumn" + currentIndexColumn);
				flag = false;
			}
			catch (ColumnIsPresentException e)
			{
				//nothing
			}
		}
		this.setDataProvider(this.providerProperty().get());
	}

	public void removeColumn(int index)
	{
		this.providerProperty().get().removeColumn(index);
		this.setDataProvider(providerProperty().get());
	}

	public void renameColumn(SpreadsheetColumn column, String text)
	{
		String oldValue = column.getText();
		try
		{
			this.providerProperty().get().setColumnName(this.columns.indexOf(column), text);
			column.setText(text);
		}
		catch (ColumnIsPresentException e)
		{
			DialogsHelper.showError(e.getMessage());
			//this.providerProperty().get().setColumnName(this.columns.indexOf(column), oldValue);
		}
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

	public void removeRow(Integer row)
	{
		this.providerProperty().get().removeRow(row);
		this.setDataProvider(this.providerProperty().get());
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

		MenuItem removeRow = new MenuItem("Remove row");
		removeRow.setOnAction(e -> removeRow(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem copyItems = new MenuItem("Copy");
		copyItems.setOnAction(event ->  copy(false));

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
		Common.saveToClipboard(copyString.toString());
	}

	private void paste(boolean withHeader)
	{
		String text = Common.getFromClipboard();
		List<TablePosition> selectedCells = this.getSelectionModel().getSelectedCells();
		if (selectedCells.size() != 1)
		{
			DialogsHelper.showInfo("Can't paste to selected cells, cause selected cell is not one");
			return;
		}
		TablePosition selectedCell = selectedCells.get(0);
		String[] rows = text.split("\n");
		int indexSelectedColumn = selectedCell.getColumn();
		int indexSelectedRow = selectedCell.getRow();

		/**
		 * add columns if needs
		 */
		int columnCount = rows[0].split("\t").length;
		List<String> columnHeaders = this.providerProperty.get().getColumnHeaders();
		if (indexSelectedColumn + columnCount > columnHeaders.size())
		{
			int addSize = indexSelectedColumn + columnCount - columnHeaders.size();
			IntStream.range(0, addSize).forEach(i -> this.providerProperty().get().addColumn(this.providerProperty().get().columnCount(), "NewColumn" + currentIndexColumn++));
			this.setDataProvider(providerProperty().get());
		}

		if (withHeader)
		{
			String[] headers = rows[0].split("\t");
			for (int i = 0; i < columnCount; i++)
			{
				SpreadsheetColumn q = this.columns.get(indexSelectedColumn + i);
				String w = headers[i];
				this.renameColumn(q, w);
			}
			/**
			 * remove 1st row from rows, because we used that string yet
			 */
			String[] newRows = new String[rows.length - 1];
			for (int i = 1; i < rows.length; i++)
			{
				newRows[i - 1] = rows[i];
			}
			rows = newRows;
		}

		/**
		 * add rows if needs
		 */
		int rowCount = rows.length;
		List<String> rowHeaders = this.providerProperty().get().getRowHeaders();
		if (indexSelectedRow + rowCount > rowHeaders.size())
		{
			int addSize = indexSelectedRow + rowCount - rowHeaders.size();
			IntStream.range(0, addSize).forEach(value -> this.providerProperty().get().addRow(this.providerProperty().get().rowCount()));
			this.setDataProvider(providerProperty().get());
		}

		for (int i = 0; i < rows.length; i++)
		{
			String[] cells = rows[i].split("\t");
			for (int j = 0; j < cells.length; j++)
			{
				this.providerProperty().get().setCellValue(indexSelectedColumn + j, indexSelectedRow + i, cells[j]);
			}
		}
		this.setDataProvider(this.getProvider());
	}

	public void deleteSelectedCells()
	{
		for (TablePosition<ObservableList<SpreadsheetCell>, ?> position : getSelectionModel().getSelectedCells())
		{
			this.providerProperty().get().setCellValue(position.getColumn(), position.getRow(), this.providerProperty.get().defaultValue());
		}
		setDataProvider(this.providerProperty().get());
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
			column.setCellValueFactory((TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> {
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

	private final EventHandler<KeyEvent> keyPressedHandler = (KeyEvent keyEvent) -> {
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
