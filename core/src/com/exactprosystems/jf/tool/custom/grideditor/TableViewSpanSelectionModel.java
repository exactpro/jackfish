////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import com.sun.javafx.collections.MappingChange;
import com.sun.javafx.collections.NonIterableChange;
import com.sun.javafx.scene.control.ReadOnlyUnbackedObservableList;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.InvalidationListener;
import javafx.beans.NamedArg;
import javafx.beans.Observable;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.event.WeakEventHandler;
import javafx.scene.control.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;
import javafx.util.Pair;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TableViewSpanSelectionModel extends TableView.TableViewSelectionModel<ObservableList<SpreadsheetCell>>
{

	private boolean shift = false; // Register state of 'shift' key
	private boolean key = false; // Register if we last touch the keyboard
	private boolean drag = false; // register if we are dragging (no
	private MouseEvent mouseEvent;
	private boolean makeAtomic;
	private SpreadsheetGridView cellsView;

	private SpreadsheetView spreadsheetView;
	private final SelectedCellsMapTemp<TablePosition<ObservableList<SpreadsheetCell>, ?>> selectedCellsMap;

	private final ReadOnlyUnbackedObservableList<TablePosition<ObservableList<SpreadsheetCell>, ?>> selectedCellsSeq;

	private Timeline timer;

	private final EventHandler<ActionEvent> timerEventHandler = (ActionEvent event) -> {
		GridViewSkin skin = (GridViewSkin) getCellsViewSkin();
		if (mouseEvent != null && !cellsView.contains(mouseEvent.getX(), mouseEvent.getY()))
		{
			double sceneX = mouseEvent.getSceneX();
			double sceneY = mouseEvent.getSceneY();
			double layoutX = cellsView.getLayoutX();
			double layoutY = cellsView.getLayoutY();
			double layoutXMax = layoutX + cellsView.getWidth();
			double layoutYMax = layoutY + cellsView.getHeight();

			if (sceneX > layoutXMax)
			{
				skin.getHBar().increment();
			}
			else if (sceneX < layoutX)
			{
				skin.getHBar().decrement();
			}
			if (sceneY > layoutYMax)
			{
				skin.getVBar().increment();
			}
			else if (sceneY < layoutY)
			{
				skin.getVBar().decrement();
			}
		}
	};
	private final EventHandler<MouseEvent> dragDoneHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent mouseEvent)
		{
			drag = false;
			timer.stop();
			spreadsheetView.removeEventHandler(MouseEvent.MOUSE_RELEASED, this);
		}
	};

	private final EventHandler<KeyEvent> keyPressedEventHandler = (KeyEvent keyEvent) -> {
		key = true;
		shift = keyEvent.isShiftDown();
	};

	private final EventHandler<MouseEvent> mousePressedEventHandler = (MouseEvent mouseEvent1) -> {
		key = false;
		shift = mouseEvent1.isShiftDown();
	};

	private final EventHandler<MouseEvent> onDragDetectedEventHandler = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent mouseEvent)
		{
			drag = true;
			timer.setCycleCount(Timeline.INDEFINITE);
			timer.play();
			cellsView.addEventHandler(MouseEvent.MOUSE_RELEASED, dragDoneHandler);
		}
	};

	private final EventHandler<MouseEvent> onMouseDragEventHandler = (MouseEvent e) -> mouseEvent = e;

	private final ListChangeListener<TablePosition<ObservableList<SpreadsheetCell>, ?>> listChangeListener = this::handleSelectedCellsListChangeEvent;

	public TableViewSpanSelectionModel(@NamedArg("spreadsheetView") SpreadsheetView spreadsheetView, @NamedArg("cellsView") SpreadsheetGridView cellsView)
	{
		super(cellsView);
		this.cellsView = cellsView;
		this.spreadsheetView = spreadsheetView;

		timer = new Timeline(new KeyFrame(Duration.millis(100), new WeakEventHandler<>((timerEventHandler))));
		cellsView.addEventHandler(KeyEvent.KEY_PRESSED, new WeakEventHandler<>(keyPressedEventHandler));

		cellsView.setOnMousePressed(new WeakEventHandler<>(mousePressedEventHandler));
		cellsView.setOnDragDetected(new WeakEventHandler<>(onDragDetectedEventHandler));

		cellsView.setOnMouseDragged(new WeakEventHandler<>(onMouseDragEventHandler));
		selectedCellsMap = new SelectedCellsMapTemp<>(new WeakListChangeListener<>(listChangeListener));

		selectedCellsSeq = new ReadOnlyUnbackedObservableList<TablePosition<ObservableList<SpreadsheetCell>, ?>>()
		{
			@Override
			public TablePosition<ObservableList<SpreadsheetCell>, ?> get(int i)
			{
				return selectedCellsMap.get(i);
			}

			@Override
			public int size()
			{
				return selectedCellsMap.size();
			}
		};
	}

	private void handleSelectedCellsListChangeEvent(ListChangeListener.Change<? extends TablePosition<ObservableList<SpreadsheetCell>, ?>> c)
	{
		if (makeAtomic)
		{
			return;
		}

		selectedCellsSeq.callObservers(new MappingChange<>(c, MappingChange.NOOP_MAP, selectedCellsSeq));
		c.reset();
	}

	private TablePosition<ObservableList<SpreadsheetCell>, ?> old = null;

	@Override
	public void select(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column)
	{
		int rowCount = this.spreadsheetView.getProvider().rowCount();
		if (row < 0 || row >= rowCount)
		{
			return;
		}
		if (isCellSelectionEnabled() && column == null)
		{
			return;
		}
		TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal = new TablePosition<>(getTableView(), row, column);

		old = posFinal;

		if (getSelectionMode() == SelectionMode.SINGLE)
		{
			quietClearSelection();
		}
		SpreadsheetCell cell = cellsView.getItems().get(old.getRow()).get(old.getColumn());
		for (int i = cell.getRow(); i < cell.getRow() + 1; ++i)
		{

			for (int j = cell.getColumn(); j < cell.getColumn() + 1; ++j)
			{
				posFinal = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
				selectedCellsMap.add(posFinal);
			}
		}

		addSelectedRowsAndColumns(old);

		setSelectedIndex(old.getRow());
		setSelectedItem(getModelItem(old.getRow()));
		if (getTableView().getFocusModel() == null)
		{
			return;
		}

		getTableView().getFocusModel().focus(old.getRow(), old.getTableColumn());
	}

	public void clearSelection(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column)
	{

		final TablePosition<ObservableList<SpreadsheetCell>, ?> tp = new TablePosition<>(getTableView(), row, column);
		if (tp.getRow() < 0 || tp.getColumn() < 0)
		{
			return;
		}
		TablePosition<ObservableList<SpreadsheetCell>, ?> position;
		if ((position = isSelectedRange(row, column, tp.getColumn())) != null)
		{
			selectedCellsMap.remove(position);
			removeSelectedRowsAndColumns(position);
			focus(position.getRow());
		}
		else
		{
			for (TablePosition<ObservableList<SpreadsheetCell>, ?> pos : getSelectedCells())
			{
				if (pos.equals(tp))
				{
					selectedCellsMap.remove(pos);
					removeSelectedRowsAndColumns(pos);
					focus(row);
					return;
				}
			}
		}
	}

	public void verifySelectedCells(List<Pair<Integer, Integer>> selectedCells)
	{
		List<TablePosition<ObservableList<SpreadsheetCell>, ?>> newList = new ArrayList<>();
		clearSelection();

		final int itemCount = getItemCount();
		final int columnSize = getTableView().getColumns().size();
		final HashSet<Integer> selectedRows = new HashSet<>();
		final HashSet<Integer> selectedColumns = new HashSet<>();
		TablePosition<ObservableList<SpreadsheetCell>, ?> pos = null;
		for (Pair<Integer, Integer> position : selectedCells)
		{
			if (position.getKey() < 0 || position.getKey() >= itemCount || position.getValue() < 0 || position.getValue() >= columnSize)
			{
				continue;
			}

			final TableColumn<ObservableList<SpreadsheetCell>, ?> column = getTableView().getVisibleLeafColumn(position.getValue());

			pos = getVisibleCell(position.getKey(), column, position.getValue());
			final SpreadsheetCell cell = cellsView.getItems().get(pos.getRow()).get(pos.getColumn());
			for (int i = cell.getRow(); i < cell.getRow() + 1; ++i)
			{

				selectedColumns.add(i);
				for (int j = cell.getColumn(); j < cell.getColumn() + 1; ++j)
				{
					selectedRows.add(j);
					pos = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
					newList.add(pos);
				}
			}
		}
		selectedCellsMap.setAll(newList);

		final TablePosition finalPos = pos;
		GridViewSkin skin = getSpreadsheetViewSkin();
		if (skin == null)
		{
			cellsView.skinProperty().addListener(new InvalidationListener()
			{

				@Override
				public void invalidated(Observable observable)
				{
					cellsView.skinProperty().removeListener(this);
					GridViewSkin skin = getSpreadsheetViewSkin();
					if (skin != null)
					{
						updateSelectedVisuals(skin, finalPos, selectedRows, selectedColumns);
					}
				}
			});
		}
		else
		{
			updateSelectedVisuals(skin, pos, selectedRows, selectedColumns);
		}
	}

	private void updateSelectedVisuals(GridViewSkin skin, TablePosition pos, HashSet<Integer> selectedRows, HashSet<Integer> selectedColumns)
	{
		if (skin != null)
		{
			skin.getSelectedRows().addAll(selectedColumns);
			skin.getSelectedColumns().addAll(selectedRows);
		}
		if (pos != null)
		{
			getCellsViewSkin().lastRowLayout.set(true);
			getCellsViewSkin().lastRowLayout.addListener(new InvalidationListener()
			{

				@Override
				public void invalidated(Observable observable)
				{
					handleSelectedCellsListChangeEvent(new NonIterableChange.SimpleAddChange<>(0, selectedCellsMap.size(), selectedCellsSeq));
					getCellsViewSkin().lastRowLayout.removeListener(this);
				}
			});
		}
	}

	@Override
	public void selectRange(int minRow, TableColumnBase<ObservableList<SpreadsheetCell>, ?> minColumn, int maxRow, TableColumnBase<ObservableList<SpreadsheetCell>, ?> maxColumn)
	{

		if (getSelectionMode() == SelectionMode.SINGLE)
		{
			quietClearSelection();
			select(maxRow, maxColumn);
			return;
		}
		SpreadsheetCell cell;

		makeAtomic = true;

		final int itemCount = getItemCount();

		final int minColumnIndex = getTableView().getVisibleLeafIndex((TableColumn<ObservableList<SpreadsheetCell>, ?>) minColumn);
		final int maxColumnIndex = getTableView().getVisibleLeafIndex((TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn);
		final int _minColumnIndex = Math.min(minColumnIndex, maxColumnIndex);
		final int _maxColumnIndex = Math.max(minColumnIndex, maxColumnIndex);

		final int _minRow = Math.min(minRow, maxRow);
		final int _maxRow = Math.max(minRow, maxRow);

		HashSet<Integer> selectedRows = new HashSet<>();
		HashSet<Integer> selectedColumns = new HashSet<>();

		for (int _row = _minRow; _row <= _maxRow; _row++)
		{
			for (int _col = _minColumnIndex; _col <= _maxColumnIndex; _col++)
			{
				if (_row < 0 || _row >= itemCount)
				{
					continue;
				}

				final TableColumn<ObservableList<SpreadsheetCell>, ?> column = getTableView().getVisibleLeafColumn(_col);

				if (column == null)
				{
					continue;
				}

				TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getVisibleCell(_row, column, _col);

				cell = cellsView.getItems().get(pos.getRow()).get(pos.getColumn());
				for (int i = cell.getRow(); i < cell.getRow() + 1; ++i)
				{

					selectedColumns.add(i);
					for (int j = cell.getColumn(); j < cell.getColumn() + 1; ++j)
					{
						selectedRows.add(j);
						pos = new TablePosition<>(getTableView(), i, getTableView().getVisibleLeafColumn(j));
						selectedCellsMap.add(pos);
					}
				}
			}
		}
		makeAtomic = false;

		getSpreadsheetViewSkin().getSelectedRows().addAll(selectedColumns);
		getSpreadsheetViewSkin().getSelectedColumns().addAll(selectedRows);

		setSelectedIndex(maxRow);
		setSelectedItem(getModelItem(maxRow));
		if (getTableView().getFocusModel() == null)
		{
			return;
		}

		getTableView().getFocusModel().focus(maxRow, (TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn);
		final int startChangeIndex = selectedCellsMap.indexOf(new TablePosition<>(getTableView(), minRow, (TableColumn<ObservableList<SpreadsheetCell>, ?>) minColumn));
		final int endChangeIndex = selectedCellsMap.getSelectedCells().size() - 1;//indexOf(new TablePosition<>(getTableView(), maxRow,
		//                (TableColumn<ObservableList<SpreadsheetCell>, ?>) maxColumn));

		if (startChangeIndex > -1 && endChangeIndex > -1)
		{
			final int startIndex = Math.min(startChangeIndex, endChangeIndex);
			final int endIndex = Math.max(startChangeIndex, endChangeIndex);
			handleSelectedCellsListChangeEvent(new NonIterableChange.SimpleAddChange<>(startIndex, endIndex + 1, selectedCellsSeq));
		}
	}

	@Override
	public void selectAll()
	{
		if (getSelectionMode() == SelectionMode.SINGLE)
		{
			return;
		}

		quietClearSelection();

		List<TablePosition<ObservableList<SpreadsheetCell>, ?>> indices = new ArrayList<>();
		TableColumn<ObservableList<SpreadsheetCell>, ?> column;
		TablePosition<ObservableList<SpreadsheetCell>, ?> tp = null;

		for (int col = 0; col < getTableView().getVisibleLeafColumns().size(); col++)
		{
			column = getTableView().getVisibleLeafColumns().get(col);
			for (int row = 0; row < getItemCount(); row++)
			{
				tp = new TablePosition<>(getTableView(), row, column);
				indices.add(tp);
			}
		}
		selectedCellsMap.setAll(indices);

		// Then we update visuals just once
		ArrayList<Integer> selectedColumns = new ArrayList<>();
		for (int col = 0; col < spreadsheetView.getProvider().columnCount(); col++)
		{
			selectedColumns.add(col);
		}

		ArrayList<Integer> selectedRows = new ArrayList<>();
		for (int row = 0; row < spreadsheetView.getProvider().rowCount(); row++)
		{
			selectedRows.add(row);
		}
		getSpreadsheetViewSkin().getSelectedRows().addAll(selectedRows);
		getSpreadsheetViewSkin().getSelectedColumns().addAll(selectedColumns);

		if (tp != null)
		{
			select(tp.getRow(), tp.getTableColumn());
			getTableView().getFocusModel().focus(0, getTableView().getColumns().get(0));
		}
	}

	@Override
	public boolean isSelected(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column)
	{
		if (column == null || row < 0)
		{
			return false;
		}

		int columnIndex = getTableView().getVisibleLeafIndex(column);

		if (getCellsViewSkin().getCellsSize() != 0)
		{
			TablePosition<ObservableList<SpreadsheetCell>, ?> posFinal = getVisibleCell(row, column, columnIndex);
			return selectedCellsMap.isSelected(posFinal.getRow(), posFinal.getColumn());
		}
		else
		{
			return selectedCellsMap.isSelected(row, columnIndex);
		}
	}

	public TablePosition<ObservableList<SpreadsheetCell>, ?> isSelectedRange(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col)
	{

		if (col < 0 || row < 0)
		{
			return null;
		}

		final SpreadsheetCell cellSpan = cellsView.getItems().get(row).get(col);
		final int infRow = cellSpan.getRow();
		final int supRow = infRow + 1;

		final int infCol = cellSpan.getColumn();
		final int supCol = infCol + 1;

		for (final TablePosition<ObservableList<SpreadsheetCell>, ?> tp : getSelectedCells())
		{
			if (tp.getRow() >= infRow && tp.getRow() < supRow && tp.getColumn() >= infCol && tp.getColumn() < supCol)
			{
				return tp;
			}
		}
		return null;
	}

	private void addSelectedRowsAndColumns(TablePosition<?, ?> position)
	{
		GridViewSkin skin = getSpreadsheetViewSkin();
		if (skin == null)
		{
			return;
		}
		final SpreadsheetCell cell = cellsView.getItems().get(position.getRow()).get(position.getColumn());
		for (int i = cell.getRow(); i < cell.getRow() + 1; ++i)
		{
			skin.getSelectedRows().add(i);
			for (int j = cell.getColumn(); j < cell.getColumn() + 1; ++j)
			{
				skin.getSelectedColumns().add(j);
			}
		}
	}

	private void removeSelectedRowsAndColumns(TablePosition<?, ?> position)
	{
		final SpreadsheetCell cell = cellsView.getItems().get(position.getRow()).get(position.getColumn());
		for (int i = cell.getRow(); i < cell.getRow() + 1; ++i)
		{
			getSpreadsheetViewSkin().getSelectedRows().remove(Integer.valueOf(i));
			for (int j = cell.getColumn(); j < cell.getColumn() + 1; ++j)
			{
				getSpreadsheetViewSkin().getSelectedColumns().remove(Integer.valueOf(j));
			}
		}
	}

	@Override
	public void clearAndSelect(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column)
	{
		makeAtomic = true;
		List<TablePosition<ObservableList<SpreadsheetCell>, ?>> previousSelection = new ArrayList<>(selectedCellsMap.getSelectedCells());

		clearSelection();
		old = null;

		select(row, column);

		makeAtomic = false;

		if (old != null && old.getColumn() >= 0)
		{
			TableColumn<ObservableList<SpreadsheetCell>, ?> columnFinal = getTableView().getColumns().get(old.getColumn());
			int changeIndex = selectedCellsSeq.indexOf(new TablePosition<>(getTableView(), old.getRow(), columnFinal));
			NonIterableChange.GenericAddRemoveChange<TablePosition<ObservableList<SpreadsheetCell>, ?>> change = new NonIterableChange.GenericAddRemoveChange<>(changeIndex, changeIndex + 1, previousSelection, selectedCellsSeq);
			handleSelectedCellsListChangeEvent(change);
		}
	}

	@Override
	public ObservableList<TablePosition> getSelectedCells()
	{
		return (ObservableList<TablePosition>) (Object) selectedCellsSeq;
	}

	@Override
	public void selectAboveCell()
	{
		final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
		if (pos.getRow() == -1)
		{
			select(getItemCount() - 1);
		}
		else if (pos.getRow() > 0)
		{
			select(pos.getRow() - 1, pos.getTableColumn());
		}

	}

	@Override
	public void selectBelowCell()
	{
		final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();

		if (pos.getRow() == -1)
		{
			select(0);
		}
		else if (pos.getRow() < getItemCount() - 1)
		{
			select(pos.getRow() + 1, pos.getTableColumn());
		}

	}

	@Override
	public void selectLeftCell()
	{
		if (!isCellSelectionEnabled())
		{
			return;
		}

		final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
		if (pos.getColumn() - 1 >= 0)
		{
			select(pos.getRow(), getTableColumn(pos.getTableColumn(), -1));
		}

	}

	@Override
	public void selectRightCell()
	{
		if (!isCellSelectionEnabled())
		{
			return;
		}

		final TablePosition<ObservableList<SpreadsheetCell>, ?> pos = getFocusedCell();
		if (pos.getColumn() + 1 < getTableView().getVisibleLeafColumns().size())
		{
			select(pos.getRow(), getTableColumn(pos.getTableColumn(), 1));
		}

	}

	@Override
	public void clearSelection()
	{
		if (!makeAtomic)
		{
			setSelectedIndex(-1);
			setSelectedItem(getModelItem(-1));
			focus(-1);
		}
		quietClearSelection();
	}

	private void quietClearSelection()
	{
		selectedCellsMap.clear();
		GridViewSkin skin = getSpreadsheetViewSkin();
		if (skin != null)
		{
			skin.getSelectedRows().clear();
			skin.getSelectedColumns().clear();
		}
	}

	@SuppressWarnings("unchecked")
	private TablePosition<ObservableList<SpreadsheetCell>, ?> getFocusedCell()
	{
		if (getTableView().getFocusModel() == null)
		{
			return new TablePosition<>(getTableView(), -1, null);
		}
		return (TablePosition<ObservableList<SpreadsheetCell>, ?>) cellsView.getFocusModel().getFocusedCell();
	}

	private TableColumn<ObservableList<SpreadsheetCell>, ?> getTableColumn(TableColumn<ObservableList<SpreadsheetCell>, ?> column, int offset)
	{
		final int columnIndex = getTableView().getVisibleLeafIndex(column);
		final int newColumnIndex = columnIndex + offset;
		return getTableView().getVisibleLeafColumn(newColumnIndex);
	}

	private GridViewSkin getSpreadsheetViewSkin()
	{
		return getCellsViewSkin();
	}


	private TablePosition<ObservableList<SpreadsheetCell>, ?> getVisibleCell(int row, TableColumn<ObservableList<SpreadsheetCell>, ?> column, int col)
	{
		return new TablePosition<>(cellsView, row, column);
	}

	final GridViewSkin getCellsViewSkin()
	{
		return (GridViewSkin) (cellsView.getSkin());
	}
}
