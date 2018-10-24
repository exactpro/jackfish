/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import com.exactprosystems.jf.tool.CssVariables;
import com.sun.javafx.scene.control.behavior.TableViewBehavior;
import com.sun.javafx.scene.control.skin.TableHeaderRow;
import com.sun.javafx.scene.control.skin.TableViewSkinBase;
import com.sun.javafx.scene.control.skin.VirtualFlow;
import javafx.beans.InvalidationListener;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Region;
import javafx.util.Callback;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.IntStream;

public class GridViewSkin extends TableViewSkinBase<ObservableList<SpreadsheetCell>, ObservableList<SpreadsheetCell>, TableView<ObservableList<SpreadsheetCell>>, TableViewBehavior<ObservableList<SpreadsheetCell>>, TableRow<ObservableList<SpreadsheetCell>>, TableColumn<ObservableList<SpreadsheetCell>, ?>>
{
	public static final double DEFAULT_CELL_HEIGHT;

	static
	{
		double cell_size = 24.0;
		try
		{
			Class<?> clazz = com.sun.javafx.scene.control.skin.CellSkinBase.class;
			Field f = clazz.getDeclaredField("DEFAULT_CELL_SIZE"); //$NON-NLS-1$
			f.setAccessible(true);
			cell_size = f.getDouble(null);
		}
		catch (NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		DEFAULT_CELL_HEIGHT = cell_size;
	}

	final Map<GridRow, Set<CellView>> deportedCells = new HashMap<>();

	ObservableMap<Integer, Double> rowHeightMap = FXCollections.observableHashMap();

	private GridCellEditor gridCellEditor;

	protected final SpreadsheetHandle handle;
	protected SpreadsheetView spreadsheetView;
	protected VerticalHeader verticalHeader;
	protected HorizontalPicker horizontalPickers;

	private final ObservableList<Integer> selectedRows = FXCollections.observableArrayList();
	private final ObservableList<Integer> selectedColumns = FXCollections.observableArrayList();
	private double fixedRowHeight = 0;
	BitSet hBarValue;
	BitSet rowToLayout;
	RectangleSelection rectangleSelection;

	double fixedColumnWidth;


	BooleanProperty lastRowLayout = new SimpleBooleanProperty(true);


	public GridViewSkin(final SpreadsheetHandle handle)
	{
		super(handle.getGridView(), new GridViewBehavior(handle.getGridView()));
		super.init(handle.getGridView());

		this.handle = handle;
		this.spreadsheetView = handle.getView();
		gridCellEditor = new GridCellEditor(handle);
		TableView<ObservableList<SpreadsheetCell>> tableView = handle.getGridView();

		tableView.setRowFactory(p -> new GridRow(handle));

		tableView.getStyleClass().add(CssVariables.CELL_SPREADSHEET); //$NON-NLS-1$

		init();

		handle.getView().providerProperty().addListener((ov, t, t1) -> rowToLayout = initRowToLayoutBitSet());
		hBarValue = new BitSet(handle.getView().getProvider().rowCount());
		rowToLayout = initRowToLayoutBitSet();

		EventHandler<MouseEvent> ml = (MouseEvent event) -> {
			if (tableView.getEditingCell() != null)
			{
				tableView.edit(-1, null);
			}

			tableView.requestFocus();
		};

		getFlow().getVerticalBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);
		getFlow().getHorizontalBar().addEventFilter(MouseEvent.MOUSE_PRESSED, ml);

		TableViewBehavior<ObservableList<SpreadsheetCell>> behavior = getBehavior();
		behavior.setOnFocusPreviousRow(this::onFocusPreviousCell);
		behavior.setOnFocusNextRow(this::onFocusNextCell);
		behavior.setOnMoveToFirstCell(this::onMoveToFirstCell);
		behavior.setOnMoveToLastCell(this::onMoveToLastCell);
		behavior.setOnScrollPageDown(this::onScrollPageDown);
		behavior.setOnScrollPageUp(this::onScrollPageUp);
		behavior.setOnSelectPreviousRow(this::onSelectPreviousCell);
		behavior.setOnSelectNextRow(this::onSelectNextCell);
		behavior.setOnSelectLeftCell(this::onSelectLeftCell);
		behavior.setOnSelectRightCell(this::onSelectRightCell);

		registerChangeListener(tableView.fixedCellSizeProperty(), "FIXED_CELL_SIZE");
	}

	public double getRowHeight(int row)
	{
		Double rowHeightCache = rowHeightMap.get(row);
		if (rowHeightCache == null)
		{
			return DEFAULT_CELL_HEIGHT;
		}
		else
		{
			return rowHeightCache;
		}
	}

	public double getFixedRowHeight()
	{
		return fixedRowHeight;
	}

	public ObservableList<Integer> getSelectedRows()
	{
		return selectedRows;
	}

	public ObservableList<Integer> getSelectedColumns()
	{
		return selectedColumns;
	}

	public GridCellEditor getSpreadsheetCellEditorImpl()
	{
		return gridCellEditor;
	}

	public GridRow getRowIndexed(int index)
	{
		List<? extends IndexedCell> cells = getFlow().getCells();
		if (!cells.isEmpty())
		{
			IndexedCell cell = cells.get(0);
			if (index >= cell.getIndex() && index - cell.getIndex() < cells.size())
			{
				return (GridRow) cells.get(index - cell.getIndex());
			}
		}
		for (IndexedCell cell : getFlow().getFixedCells())
		{
			if (cell.getIndex() == index)
			{
				return (GridRow) cell;
			}
		}
		return null;
	}

	public GridRow getRow(int index)
	{
		return (GridRow) getFlow().getCells().get(index);
	}

	public int getCellsSize()
	{
		return getFlow().getCells().size();
	}

	public ScrollBar getHBar()
	{
		return getFlow().getHorizontalBar();
	}

	public ScrollBar getVBar()
	{
		return getFlow().getVerticalBar();
	}

	public void resizeRowsToFitContent()
	{
		int maxRows = handle.getView().getProvider().rowCount();
		for (int row = 0; row < maxRows; row++)
		{
			resizeRowToFitContent(row);
		}
	}

	public void resizeRowToFitContent(int row)
	{
		if (getSkinnable().getColumns().isEmpty())
		{
			return;
		}
		final TableColumn<ObservableList<SpreadsheetCell>, ?> col = getSkinnable().getColumns().get(0);
		List<?> items = itemsProperty().get();
		if (items == null || items.isEmpty())
		{
			return;
		}

		Callback/* <TableColumn<T, ?>, TableCell<T,?>> */ cellFactory = col.getCellFactory();
		if (cellFactory == null)
		{
			return;
		}

		CellView cell = (CellView) cellFactory.call(col);
		if (cell == null)
		{
			return;
		}

		// set this property to tell the TableCell we want to know its actual
		// preferred width, not the width of the associated TableColumnBase
		cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE); //$NON-NLS-1$

		// determine cell padding
		double padding = 5;

		Node n = cell.getSkin() == null ? null : cell.getSkin().getNode();
		if (n instanceof Region)
		{
			Region r = (Region) n;
			padding = r.snappedTopInset() + r.snappedBottomInset();
		}

		double maxHeight;
		maxHeight = 18;
		getChildren().add(cell);

		for (TableColumn column : getSkinnable().getColumns())
		{
			cell.updateTableColumn(column);
			cell.updateTableView(handle.getGridView());
			cell.updateIndex(row);

			if ((cell.getText() != null && !cell.getText().isEmpty()) || cell.getGraphic() != null)
			{
				cell.setWrapText(true);

				cell.applyCss();
				maxHeight = Math.max(maxHeight, cell.prefHeight(column.getWidth()));
			}
		}
		getChildren().remove(cell);
		rowHeightMap.put(row, maxHeight + padding);
		Event.fireEvent(spreadsheetView, new SpreadsheetView.RowHeightEvent(row, maxHeight + padding));

		rectangleSelection.updateRectangle();
	}

	@Override
	public void resizeColumnToFitContent(TableColumn<ObservableList<SpreadsheetCell>, ?> tc, int maxRows)
	{

		List<?> items = itemsProperty().get();
		if (items == null || items.isEmpty())
		{
			return;
		}

		Callback/* <TableColumn<T, ?>, TableCell<T,?>> */ cellFactory = tc.getCellFactory();
		if (cellFactory == null)
		{
			return;
		}

		TableCell<ObservableList<SpreadsheetCell>, ?> cell = (TableCell<ObservableList<SpreadsheetCell>, ?>) cellFactory.call(tc);
		if (cell == null)
		{
			return;
		}

		int indexColumn = handle.getGridView().getColumns().indexOf(tc);

		if (maxRows == 30 && handle.isColumnWidthSet(indexColumn))
		{
			return;
		}

		cell.getProperties().put("deferToParentPrefWidth", Boolean.TRUE); //$NON-NLS-1$

		int rows = maxRows == -1 ? items.size() : Math.min(items.size(), maxRows == 30 ? 100 : maxRows);
		cell.updateTableColumn(tc);
		cell.updateTableView(handle.getGridView());

		int column = IntStream.range(0, spreadsheetView.getColumns().size())
				.filter(i -> spreadsheetView.getColumns().get(i).getText().equals(tc.getText()))
				.findFirst()
				.getAsInt();

		double maxWidth = IntStream.range(0, rows)
				.mapToObj(row -> (String) spreadsheetView.getProvider().getCellValue(column, row))
				.mapToDouble(cellValue -> cellValue.length() * 8 + 30)
				.max()
				.getAsDouble();

		cell.updateIndex(-1);
		double widthMax = Math.max(tc.getText().length() * 7 + 20, maxWidth);
		if (handle.getGridView().getColumnResizePolicy() == TableView.CONSTRAINED_RESIZE_POLICY)
		{
			widthMax = Math.max(widthMax, tc.getWidth());
		}
		widthMax = snapSize(widthMax);
		if (tc.getPrefWidth() == widthMax && tc.getWidth() != widthMax)
		{
			tc.impl_setWidth(widthMax);
		}
		else
		{
			tc.setPrefWidth(widthMax);
		}

		resizeRowsToFitContent();
		rectangleSelection.updateRectangle();
	}

	protected final void init()
	{
		rectangleSelection = new RectangleSelection(this, (TableViewSpanSelectionModel) handle.getGridView().getSelectionModel());
		getFlow().getVerticalBar().valueProperty().addListener(vbarValueListener);
		verticalHeader = new VerticalHeader(handle);
		getChildren().add(verticalHeader);

		((HorizontalHeader) getTableHeaderRow()).init();
		verticalHeader.init(this, (HorizontalHeader) getTableHeaderRow());

		horizontalPickers = new HorizontalPicker((HorizontalHeader) getTableHeaderRow(), spreadsheetView);
		getChildren().add(horizontalPickers);
		getFlow().init(spreadsheetView);
		((GridViewBehavior) getBehavior()).setGridViewSkin(this);
	}

	public RectangleSelection getRectangleSelection()
	{
		return rectangleSelection;
	}

	public void resize(TableColumnBase<?, ?> tc, int maxRows)
	{
		if (tc.isResizable())
		{
			int columnIndex = getColumns().indexOf(tc);
			TableColumn tableColumn = getColumns().get(columnIndex);
			resizeColumnToFitContent(tableColumn, maxRows);
			Event.fireEvent(spreadsheetView, new SpreadsheetView.ColumnWidthEvent(columnIndex, tableColumn.getWidth()));
		}
	}

	@Override
	protected void layoutChildren(double x, double y, double w, final double h)
	{
		if (spreadsheetView == null)
		{
			return;
		}
		double verticalHeaderWidth = verticalHeader.computeHeaderWidth();
		double horizontalPickerHeight = spreadsheetView.getColumnPickers().isEmpty() ? 0 : VerticalHeader.PICKER_SIZE;

		x += verticalHeaderWidth;
		w -= verticalHeaderWidth;

		y += horizontalPickerHeight;
		super.layoutChildren(x, y, w, h - horizontalPickerHeight);

		final double baselineOffset = getSkinnable().getLayoutBounds().getHeight() / 2;
		double tableHeaderRowHeight = 0;

		if (!spreadsheetView.getColumnPickers().isEmpty())
		{
			layoutInArea(horizontalPickers, x, y - VerticalHeader.PICKER_SIZE, w, tableHeaderRowHeight, baselineOffset, HPos.CENTER, VPos.CENTER);
		}

		tableHeaderRowHeight = getTableHeaderRow().prefHeight(-1);
		layoutInArea(getTableHeaderRow(), x, y, w, tableHeaderRowHeight, baselineOffset, HPos.CENTER, VPos.CENTER);

		y += tableHeaderRowHeight;

		layoutInArea(verticalHeader, x - verticalHeaderWidth, y - tableHeaderRowHeight, w, h, baselineOffset, HPos.CENTER, VPos.CENTER);
	}

	@Override
	protected void onFocusPreviousCell()
	{
		focusScroll();
	}

	@Override
	protected void onFocusNextCell()
	{
		focusScroll();
	}

	void focusScroll()
	{
		final TableFocusModel<?, ?> fm = getFocusModel();
		if (fm == null)
		{
			return;
		}
		final int row = fm.getFocusedIndex();
		if (!getFlow().getCells().isEmpty() && getFlow().getCells().get(0).getIndex() > row)
		{
			flow.scrollTo(row);
		}
		else
		{
			flow.show(row);
		}
		scrollHorizontally();
	}

	@Override
	protected void onSelectPreviousCell()
	{
		super.onSelectPreviousCell();
		scrollHorizontally();
	}

	@Override
	protected void onSelectNextCell()
	{
		super.onSelectNextCell();
		scrollHorizontally();
	}

	@Override
	protected VirtualFlow<TableRow<ObservableList<SpreadsheetCell>>> createVirtualFlow()
	{
		return new GridVirtualFlow<>(this);
	}

	@Override
	protected TableHeaderRow createTableHeaderRow()
	{
		return new HorizontalHeader(this);
	}

	protected HorizontalHeader getHorizontalHeader()
	{
		return (HorizontalHeader) getTableHeaderRow();
	}

	@Override
	public void scrollHorizontally()
	{
		super.scrollHorizontally();
	}

	@Override
	protected void scrollHorizontally(TableColumn<ObservableList<SpreadsheetCell>, ?> col)
	{
		if (col == null || !col.isVisible())
		{
			return;
		}

		fixedColumnWidth = 0;
		final double pos = getFlow().getHorizontalBar().getValue();
		int index = getColumns().indexOf(col);
		double start = 0;

		for (int i = 0; i < index; ++i)
		{
			SpreadsheetColumn column = spreadsheetView.getColumns().get(i);
			start += column.getWidth();
		}

		final double end = start + col.getWidth();

		final double headerWidth = handle.getView().getWidth() - snappedLeftInset() - snappedRightInset() - verticalHeader.getVerticalHeaderWidth();

		final double max = getFlow().getHorizontalBar().getMax();
		double newPos;

		if (start < pos + fixedColumnWidth && start >= 0 && start >= fixedColumnWidth)
		{
			newPos = start - fixedColumnWidth < 0 ? start : start - fixedColumnWidth;
			getFlow().getHorizontalBar().setValue(newPos);
		}
		else if (start > pos + headerWidth)
		{
			final double delta = start < 0 || end > headerWidth ? start - pos - fixedColumnWidth : 0;
			newPos = pos + delta > max ? max : pos + delta;
			getFlow().getHorizontalBar().setValue(newPos);
		}
	}

	private void verticalScroll()
	{
		verticalHeader.requestLayout();
	}

	GridVirtualFlow<?> getFlow()
	{
		return (GridVirtualFlow<?>) flow;
	}

	private BitSet initRowToLayoutBitSet()
	{
		DataProvider provider = handle.getView().getProvider();
		return new BitSet(provider.rowCount());
	}

	private final InvalidationListener vbarValueListener = valueModel -> verticalScroll();


	@Override
	protected TableSelectionModel<ObservableList<SpreadsheetCell>> getSelectionModel()
	{
		return getSkinnable().getSelectionModel();
	}

	@Override
	protected TableFocusModel<ObservableList<SpreadsheetCell>, TableColumn<ObservableList<SpreadsheetCell>, ?>> getFocusModel()
	{
		return getSkinnable().getFocusModel();
	}

	@Override
	protected TablePositionBase<? extends TableColumn<ObservableList<SpreadsheetCell>, ?>> getFocusedCell()
	{
		return getSkinnable().getFocusModel().getFocusedCell();
	}

	@Override
	protected ObservableList<? extends TableColumn<ObservableList<SpreadsheetCell>, ?>> getVisibleLeafColumns()
	{
		return getSkinnable().getVisibleLeafColumns();
	}

	@Override
	protected int getVisibleLeafIndex(TableColumn<ObservableList<SpreadsheetCell>, ?> tc)
	{
		return getSkinnable().getVisibleLeafIndex(tc);
	}

	@Override
	protected TableColumn<ObservableList<SpreadsheetCell>, ?> getVisibleLeafColumn(int col)
	{
		return getSkinnable().getVisibleLeafColumn(col);
	}

	@Override
	protected ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> getColumns()
	{
		return getSkinnable().getColumns();
	}

	@Override
	protected ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> getSortOrder()
	{
		return getSkinnable().getSortOrder();
	}

	@Override
	protected ObjectProperty<ObservableList<ObservableList<SpreadsheetCell>>> itemsProperty()
	{
		return getSkinnable().itemsProperty();
	}

	@Override
	protected ObjectProperty<Callback<TableView<ObservableList<SpreadsheetCell>>, TableRow<ObservableList<SpreadsheetCell>>>> rowFactoryProperty()
	{
		return getSkinnable().rowFactoryProperty();
	}

	@Override
	protected ObjectProperty<Node> placeholderProperty()
	{
		return getSkinnable().placeholderProperty();
	}

	@Override
	protected BooleanProperty tableMenuButtonVisibleProperty()
	{
		return getSkinnable().tableMenuButtonVisibleProperty();
	}

	@Override
	protected ObjectProperty<Callback<ResizeFeaturesBase, Boolean>> columnResizePolicyProperty()
	{
		return (ObjectProperty<Callback<ResizeFeaturesBase, Boolean>>) (Object) getSkinnable().columnResizePolicyProperty();
	}

	@Override
	protected boolean resizeColumn(TableColumn<ObservableList<SpreadsheetCell>, ?> tc, double delta)
	{
		getHorizontalHeader().getRootHeader().lastColumnResized = getColumns().indexOf(tc);
		boolean returnedValue = getSkinnable().resizeColumn(tc, delta);
		if (returnedValue)
		{
			Event.fireEvent(spreadsheetView, new SpreadsheetView.ColumnWidthEvent(getColumns().indexOf(tc), tc.getWidth()));
		}
		return returnedValue;
	}

	@Override
	protected void edit(int index, TableColumn<ObservableList<SpreadsheetCell>, ?> column)
	{
		getSkinnable().edit(index, column);
	}

	@Override
	public TableRow<ObservableList<SpreadsheetCell>> createCell()
	{
		TableRow<ObservableList<SpreadsheetCell>> cell;

		if (getSkinnable().getRowFactory() != null)
		{
			cell = getSkinnable().getRowFactory().call(getSkinnable());
		}
		else
		{
			cell = new TableRow<>();
		}

		cell.updateTableView(getSkinnable());
		return cell;
	}

	@Override
	public int getItemCount()
	{
		return getSkinnable().getItems() == null ? 0 : getSkinnable().getItems().size();
	}
}
