////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class SpreadsheetView extends Control{

    private static final double DEFAULT_ROW_HEADER_WIDTH = 30.0;

    private final SpreadsheetGridView cellsView;// The main cell container.
	private SimpleObjectProperty<DataProvider> providerProperty = new SimpleObjectProperty<>();

    private final BooleanProperty fixingRowsAllowedProperty = new SimpleBooleanProperty(true);

    private BitSet rowFix; // Compute if we can fix the rows or not.

    private final ObservableMap<Integer, Picker> rowPickers = FXCollections.observableHashMap();

    private final ObservableMap<Integer, Picker> columnPickers = FXCollections.observableHashMap();

    private ObservableList<SpreadsheetColumn> columns = FXCollections.observableArrayList();
    private Map<StringCellType, SpreadsheetCellEditor> editors = new IdentityHashMap<>();
    private final SpreadsheetViewSelectionModel selectionModel;

    private final DoubleProperty rowHeaderWidth = new SimpleDoubleProperty(DEFAULT_ROW_HEADER_WIDTH);

    private final BitSet columnWidthSet = new BitSet();

	final SpreadsheetHandle handle = new SpreadsheetHandle() {

        @Override
        protected SpreadsheetView getView() {
            return SpreadsheetView.this;
        }

        @Override
        protected GridViewSkin getCellsViewSkin() {
            return SpreadsheetView.this.getCellsViewSkin();
        }

        @Override
        protected SpreadsheetGridView getGridView() {
            return SpreadsheetView.this.getCellsView();
        }

        @Override
        protected boolean isColumnWidthSet(int indexColumn) {
            return columnWidthSet.get(indexColumn);
        }
    };

    final GridViewSkin getCellsViewSkin() {
        return (GridViewSkin) (cellsView.getSkin());
    }

    final SpreadsheetGridView getCellsView() {
        return cellsView;
    }

    void columnWidthSet(int indexColumn) {
        columnWidthSet.set(indexColumn);
    }

	/***************************************************************************
	 * * Constructor * *
	 **************************************************************************/

	public SpreadsheetView(DataProvider provider)
	{
		super();
//		addEventHandler(RowHeightEvent.ROW_HEIGHT_CHANGE, (RowHeightEvent event) -> {
//			if(getFixedRows().contains(event.getRow()) && getCellsViewSkin() != null){
//				getCellsViewSkin().computeFixedRowHeight();
//			}
//		});
		getStyleClass().add("SpreadsheetView"); //$NON-NLS-1$
		// anonymous skin
		setSkin(new Skin<SpreadsheetView>() {
			@Override
			public Node getNode() {
				return SpreadsheetView.this.getCellsView();
			}

			@Override
			public SpreadsheetView getSkinnable() {
				return SpreadsheetView.this;
			}

			@Override
			public void dispose() {
				// no-op
			}
		});

		this.cellsView = new SpreadsheetGridView(handle);

		getChildren().add(cellsView);

		/**
		 * Add a listener to the selection model in order to edit the spanned
		 * cells when clicked
		 */
		TableViewSpanSelectionModel tableViewSpanSelectionModel = new TableViewSpanSelectionModel(this,cellsView);
		cellsView.setSelectionModel(tableViewSpanSelectionModel);
		tableViewSpanSelectionModel.setCellSelectionEnabled(true);
		tableViewSpanSelectionModel.setSelectionMode(SelectionMode.MULTIPLE);
		selectionModel = new SpreadsheetViewSelectionModel(this, tableViewSpanSelectionModel);

		/**
		 * Set the focus model to track keyboard change and redirect focus on
		 * spanned cells
		 */
		// We add a listener on the focus model in order to catch when we are on
		// a hidden cell
		cellsView.getFocusModel().focusedCellProperty()
				.addListener((ChangeListener<TablePosition>) (ChangeListener<?>) new FocusModelListener(this,cellsView));

		/**
		 * Keyboard action, maybe use an accelerator
		 */
		cellsView.setOnKeyPressed(keyPressedHandler);

		setContextMenu(getSpreadsheetViewContextMenu());

		setDataProvider(provider);
		setEditable(true);

		// Listeners & handlers
	}

	/***************************************************************************
	 * * Public Methods * *
	 **************************************************************************/

	public final void setDataProvider(DataProvider provider)
	{
		if(provider == null){
			return;
		}
		providerProperty.set(provider);
		List<Double> widthColumns = columns.stream().map(SpreadsheetColumn::getWidth).collect(Collectors.toList());
		//We need to update the focused cell afterwards
		Pair<Integer, Integer> focusedPair = null;
		TablePosition focusedCell = cellsView.getFocusModel().getFocusedCell();
		if (focusedCell != null && focusedCell.getRow() != -1 && focusedCell.getColumn() != -1) {
			focusedPair = new Pair<>(focusedCell.getRow(), focusedCell.getColumn());
		}

		final Pair<Integer, Integer> finalPair = focusedPair;

		if (provider.columnCount() > 0 && provider.rowCount() > 0) {
			final ObservableList<ObservableList<SpreadsheetCell>> observableRows = FXCollections.observableArrayList(provider.getRows());
			cellsView.getItems().clear();
			cellsView.setItems(observableRows);

			final int columnCount = provider.columnCount();
			columns.clear();
			for (int columnIndex = 0; columnIndex < columnCount; ++columnIndex) {
				final SpreadsheetColumn spreadsheetColumn = new SpreadsheetColumn(getTableColumn(provider, columnIndex), this, columnIndex, provider);
				if(widthColumns.size() > columnIndex){
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
			//Just like the selected cell we update the focused cell.
			if (finalPair != null && finalPair.getKey() < getProvider().rowCount() && finalPair.getValue() < getProvider().columnCount())
			{
				cellsView.getFocusModel().focus(finalPair.getKey(), cellsView.getColumns().get(finalPair.getValue()));
			}
		};

		if (Platform.isFxApplicationThread()) {
			runnable.run();
		} else {
			try {
				FutureTask future = new FutureTask(runnable, null);
				Platform.runLater(future);
				future.get();
			} catch (InterruptedException | ExecutionException ex) {
				Logger.getLogger(SpreadsheetView.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public void addColumn(int index)
	{
		//TODO add implementation
		this.providerProperty().get().addColumn(index, "NewColumn");
		this.setDataProvider(this.providerProperty().get());
	}

	public void removeColumn(int index)
	{
		//TODO add implementation
		this.providerProperty().get().removeColumn(index);
		this.setDataProvider(providerProperty().get());
	}

	public void renameColumn(SpreadsheetColumn column, String text)
	{
		column.setText(text);
		this.providerProperty().get().setColumnName(this.columns.indexOf(column), text);
	}

	public void addRowBefore(Integer row)
	{
		//TODO add implementation
		this.providerProperty().get().addRow(row);
		this.setDataProvider(this.providerProperty().get());
	}

	public void addRowAfter(Integer row)
	{
		//TODO add implementation
		this.providerProperty().get().addRow(row + 1);
		this.setDataProvider(this.providerProperty().get());
	}

	public void removeRow(Integer row)
	{
		//TODO add implementation
		this.providerProperty().get().removeRow(row);
		this.setDataProvider(this.providerProperty().get());
	}

    /**
     * Return a {@link TablePosition} of cell being currently edited.
     *
     * @return a {@link TablePosition} of cell being currently edited.
     */
    public TablePosition<ObservableList<SpreadsheetCell>, ?> getEditingCell() {
        return cellsView.getEditingCell();
    }

    public final ObservableList<SpreadsheetColumn> getColumns() {
        return columns;
    }

    /**
     * Return the model Grid used by the SpreadsheetView
     *
     * @return the model Grid used by the SpreadsheetView
     */
	public final DataProvider getProvider()
	{
		return this.providerProperty.get();
	}

	public final ReadOnlyObjectProperty<DataProvider> providerProperty() {
		return providerProperty;
	}

    public boolean isRowFixable(int row) {
        return row >= 0 && row < rowFix.size() && rowFix.get(row);
    }


    /**
     * This DoubleProperty represents the with of the rowHeader. This is just
     * representing the width of the Labels, not the pickers.
     *
     * @return A DoubleProperty.
     */
    public final DoubleProperty rowHeaderWidthProperty(){
        return rowHeaderWidth;
    }

    /**
     *
     * @return the current width of the row header.
     */
    public final double getRowHeaderWidth(){
        return rowHeaderWidth.get();
    }

    /**
     * @return An ObservableMap with the row index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getRowPickers() {
        return rowPickers;
    }

    /**
     * @return An ObservableMap with the column index as key and the Picker as a
     * value.
     */
    public ObservableMap<Integer, Picker> getColumnPickers() {
        return columnPickers;
    }

    /**
     * This method will compute the best height for each line. That is to say
     * a height where each content of each cell could be fully visible.\n
     * Use this method wisely because it can degrade performance on great grid.
     */
    public void resizeRowsToFitContent(){
        getCellsViewSkin().resizeRowsToFitContent();
    }

    /**
     * This method will first apply {@link #resizeRowsToFitContent() } and then
     * take the highest height and apply it to every row.\n
     * Just as {@link #resizeRowsToFitContent() }, this method can be degrading
     * your performance on great grid.
     */
    public void resizeRowsToMaximum(){
        getCellsViewSkin().resizeRowsToMaximum();
    }

    /**
     * This method will wipe all changes made to the row's height and set all row's
     * height back to their default height defined in the model Grid.
     */
    public void resizeRowsToDefault(){
        getCellsViewSkin().resizeRowsToDefault();
    }

    /**
     * @param row
     * @return the height of a particular row of the SpreadsheetView.
     */
    public double getRowHeight(int row) {
        //Sometime, the skin is not initialised yet..
        if (getCellsViewSkin() == null)
		{
			return -1;
		} else {
            return getCellsViewSkin().getRowHeight(row);
        }
    }

    /**
     * Return the selectionModel used by the SpreadsheetView.
     *
     * @return {@link SpreadsheetViewSelectionModel}
     */
    public SpreadsheetViewSelectionModel getSelectionModel() {
        return selectionModel;
    }

    /**
     * Scrolls the SpreadsheetView so that the given row is visible.
     * @param row
     */
    public void scrollToRow(int row){
        cellsView.scrollTo(row);
    }

    /**
     * Scrolls the SpreadsheetView so that the given {@link SpreadsheetColumn} is visible.
     * @param column
     */
    public void scrollToColumn(SpreadsheetColumn column){
        cellsView.scrollToColumn(column.column);
    }

    /**
     *
     * Scrolls the SpreadsheetView so that the given column index is visible.
     *
     * @param columnIndex
     *
     */
    public void scrollToColumnIndex(int columnIndex) {
        cellsView.scrollToColumnIndex(columnIndex);
    }

    /**
     * Return the editor associated with the CellType. (defined in
     * {@link SpreadsheetCellType#createEditor(SpreadsheetView)}. FIXME Maybe
     * keep the editor references inside the SpreadsheetCellType
     *
     * @param cellType
     * @return the editor associated with the CellType.
     */
    public final Optional<SpreadsheetCellEditor> getEditor(StringCellType cellType) {
        if(cellType == null){
            return Optional.empty();
        }
        SpreadsheetCellEditor cellEditor = editors.get(cellType);
        if (cellEditor == null) {
            cellEditor = cellType.createEditor(this);
            if(cellEditor == null){
                return Optional.empty();
            }
            editors.put(cellType, cellEditor);
        }
        return Optional.of(cellEditor);
    }

    /**
     * Sets the value of the property editable.
     *
     * @param b
     */
    public final void setEditable(final boolean b) {
        cellsView.setEditable(b);
    }

    /**
     * Gets the value of the property editable.
     *
     * @return a boolean telling if the SpreadsheetView is editable.
     */
    public final boolean isEditable() {
        return cellsView.isEditable();
    }

    /**
     * Specifies whether this SpreadsheetView is editable - only if the
     * SpreadsheetView, and the {@link SpreadsheetCell} within it are both
     * editable will a {@link SpreadsheetCell} be able to go into its editing
     * state.
     *
     * @return the BooleanProperty associated with the editableProperty.
     */
    public final BooleanProperty editableProperty() {
        return cellsView.editableProperty();
    }


    public ContextMenu getSpreadsheetViewContextMenu() {
        final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);
		MenuItem addRowBefore = new MenuItem("Add before");
		addRowBefore.setOnAction(e -> addRowBefore(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem addRowAfter = new MenuItem("Add after");
		addRowAfter.setOnAction(e -> addRowAfter(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		MenuItem removeRow = new MenuItem("Remove row");
		removeRow.setOnAction(e -> removeRow(this.getSelectionModel().getSelectedCells().get(0).getRow()));

		contextMenu.getItems().addAll(addRowBefore, addRowAfter, removeRow);
        return contextMenu;
    }

    /**
     * This method is called when pressing the "delete" key on the
     * SpreadsheetView. This will erase the values of selected cells. This can
     * be overridden by developers for custom behavior.
     */
    public void deleteSelectedCells() {
        for (TablePosition<ObservableList<SpreadsheetCell>, ?> position : getSelectionModel().getSelectedCells()) {
			this.providerProperty().get().setCellValue(position.getColumn(), position.getRow(), null);
        }
		setDataProvider(this.providerProperty().get());
    }

    /***************************************************************************
     * * Private/Protected Implementation * *
     **************************************************************************/

	private TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> getTableColumn(DataProvider provider, int columnIndex) {

		TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> column;

		String columnHeader = provider.getColumnHeaders().size() > columnIndex ? String.valueOf(provider.getColumnHeaders().get(columnIndex)) : "new";

		if (columnIndex < cellsView.getColumns().size()) {
			column = (TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell>) cellsView.getColumns().get(columnIndex);
			column.setText(columnHeader);
		} else {
			column = new TableColumn<>(columnHeader);

			column.setEditable(true);
			column.setSortable(false);
			column.setCellValueFactory((TableColumn.CellDataFeatures<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> {
				if (columnIndex >= p.getValue().size()) {
					return null;
				}
				return new ReadOnlyObjectWrapper<>(p.getValue().get(columnIndex));
			});
			column.setCellFactory((TableColumn<ObservableList<SpreadsheetCell>, SpreadsheetCell> p) -> new CellView(handle));
		}
		return column;
	}

    /**
     * This is called when setting a Grid. The main idea is to re-use
     * TableColumn if possible. Because we can have a great amount of time spent
     * in com.sun.javafx.css.StyleManager.forget when removing lots of columns
     * and adding new ones. So if we already have some, we can just re-use them
     * so we avoid doign all the fuss with the TableColumns.
     *
     * @param grid
     * @param columnIndex
     * @return
     */
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
				//We consume the event because we don't want to go in edition
				keyEvent.consume();
			}
			getCellsViewSkin().scrollHorizontally();
			// Go to next cell
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
			//We consume the event because we don't want to loose focus
			keyEvent.consume();
			getCellsViewSkin().scrollHorizontally();
			// We want to erase values when delete key is pressed.
		}
		else if (KeyCode.DELETE.equals(keyEvent.getCode()))
		{
			deleteSelectedCells();
			/**
			 * We want NOT to go in edition if we're pressing SHIFT and if we're
			 * using the navigation keys. But we still want the user to go in
			 * edition with SHIFT and some letters for example if he wants a
			 * capital letter.
			 * FIXME Add a test to prevent the Shift fail case.
			 */
		}
		else if (keyEvent.getCode() != KeyCode.SHIFT && !keyEvent.isShortcutDown() && !keyEvent.getCode().isNavigationKey() && keyEvent.getCode() != KeyCode.ESCAPE)
		{
			getCellsView().edit(position.getRow(), position.getTableColumn());
		}
	};
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a row
     * with its mouse.
     */
    public static class RowHeightEvent extends Event {

        /**
         * This is the event used by {@link RowHeightEvent}.
         */
        public static final EventType<RowHeightEvent> ROW_HEIGHT_CHANGE = new EventType<>(Event.ANY, "RowHeightChange"); //$NON-NLS-1$

        private final int row;
        private final double height;

        public RowHeightEvent(int row, double height) {
            super(ROW_HEIGHT_CHANGE);
            this.row = row;
            this.height = height;
        }

        /**
         * Return the row index that has been resized.
         * @return the row index that has been resized.
         */
        public int getRow() {
            return row;
        }

        /**
         * Return the new height for this row.
         * @return the new height for this row.
         */
        public double getHeight() {
            return height;
        }
    }
    
    /**
     * This event is thrown on the SpreadsheetView when the user resize a column
     * with its mouse.
     */
    public static class ColumnWidthEvent extends Event {

        /**
         * This is the event used by {@link ColumnWidthEvent}.
         */
        public static final EventType<ColumnWidthEvent> COLUMN_WIDTH_CHANGE = new EventType<>(Event.ANY, "ColumnWidthChange"); //$NON-NLS-1$

        private final int column;
        private final double width;

        public ColumnWidthEvent(int column, double width) {
            super(COLUMN_WIDTH_CHANGE);
            this.column = column;
            this.width = width;
        }

        /**
         * Return the column index that has been resized.
         * @return the column index that has been resized.
         */
        public int getColumn() {
            return column;
        }

        /**
         * Return the new width for this column.
         * @return the new width for this column.
         */
        public double getWidth() {
            return width;
        }
    }
}
