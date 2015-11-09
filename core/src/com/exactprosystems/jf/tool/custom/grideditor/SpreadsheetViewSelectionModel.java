////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.ObservableList;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TableView;
import javafx.util.Pair;

import java.util.Arrays;
import java.util.List;

/**
 *
 * This class provides basic support for common interaction on the
 * {@link SpreadsheetView}.
 *
 * Due to the complexity induced by cell's span, it is not possible to give a
 * full access to selectionModel like in the {@link TableView}.
 */
public class SpreadsheetViewSelectionModel {

    private final TableViewSpanSelectionModel selectionModel;
    private final SpreadsheetView spv;

    SpreadsheetViewSelectionModel(SpreadsheetView spv, TableViewSpanSelectionModel selectionModel) {
        this.spv = spv;
        this.selectionModel = selectionModel;
    }
    
    /**
     * Clears all selection, and then selects the cell at the given row/column intersection.
     * @param row
     * @param column 
     */
    public final void clearAndSelect(int row, SpreadsheetColumn column) {
        selectionModel.clearAndSelect(row, column.column);
    }
    
    /**
     * Selects the cell at the given row/column intersection.
     * @param row
     * @param column 
     */
    public final void select(int row, SpreadsheetColumn column) {
        selectionModel.select(row,column.column);
    }
    
    /**
     * Clears the selection model of all selected indices.
     */
    public final void clearSelection() {
        selectionModel.clearSelection();
    }
    
    /**
     * A read-only ObservableList representing the currently selected cells in this SpreadsheetView. 
     * @return A read-only ObservableList.
     */
    public final ObservableList<TablePosition> getSelectedCells() {
        return selectionModel.getSelectedCells();
    }
    
    /**
     * Select all the possible cells.
     */
    public final void selectAll() {
        selectionModel.selectAll();
    }
    
    /**
     * Return the position of the cell that has current focus. 
     * @return the position of the cell that has current focus. 
     */
    public final TablePosition getFocusedCell(){
        return selectionModel.getTableView().getFocusModel().getFocusedCell();
    }
    
    /**
     * Causes the cell at the given index to receive the focus.
     * @param row The row index of the item to give focus to.
     * @param column The column of the item to give focus to. Can be null.
     */
    public final void focus(int row, SpreadsheetColumn column){
        selectionModel.getTableView().getFocusModel().focus(row, column.column);
    }
    
    /**
     * Specifies the selection mode to use in this selection model. The
     * selection mode specifies how many items in the underlying data model can
     * be selected at any one time. By default, the selection mode is
     * {@link SelectionMode#MULTIPLE}.
     *
     * @param value
     */
    public final void setSelectionMode(SelectionMode value) {
        selectionModel.setSelectionMode(value);
    }
    
    /**
     * Return the selectionMode currently used.
     *
     * @return the selectionMode currently used.
     */
    public SelectionMode getSelectionMode() {
        return selectionModel.getSelectionMode();
    }
    
    
    /**
     * Use this method to select discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, SpreadsheetColumn) }.
     *
     * @param selectedCells
     */
    public void selectCells(List<Pair<Integer, Integer>> selectedCells) {
        selectionModel.verifySelectedCells(selectedCells);
    }

    /**
     * Use this method to select discontinuous cells.
     *
     * The {@link Pair} must contain the row index as key and the column index
     * as value. This is useful when you want to select a great amount of cell
     * because it will be more efficient than calling
     * {@link #select(int, SpreadsheetColumn) }.
     * @param selectedCells
     */
    public void selectCells(Pair<Integer, Integer>... selectedCells) {
        selectionModel.verifySelectedCells(Arrays.asList(selectedCells));
    }

    /**
     * Selects the cells in the range (minRow, minColumn) to (maxRow, maxColumn), inclusive.
     * @param minRow
     * @param minColumn
     * @param maxRow
     * @param maxColumn
     */
    public void selectRange(int minRow, SpreadsheetColumn minColumn, int maxRow, SpreadsheetColumn maxColumn) {
        selectionModel.selectRange(minRow, minColumn.column, maxRow, maxColumn.column);
    }

    /**
     * Clear the current selection and select the cell on the left of the
     * current focused cell. If the cell is the first one on a row, the last
     * cell of the preceding row is selected.
     */
    public void clearAndSelectLeftCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int row = position.getRow();
        int column = position.getColumn();
        column -= 1;
        if (column < 0) {
            if (row == 0) {
                column++;
            } else {
                column = spv.getProvider().columnCount() - 1;
                row--;
            }
        }
        clearAndSelect(row, spv.getColumns().get(column));
    }

    /**
     * Clear the current selection and select the cell on the right of the
     * current focused cell. If the cell is the last one on a row, the first
     * cell of the next row is selected.
     */
    public void clearAndSelectRightCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int row = position.getRow();
        int column = position.getColumn();
        column += 1;
        if (column >= spv.getColumns().size()) {
            if (row == spv.getProvider().rowCount() - 1) {
                column--;
            } else {
                column = 0;
                row++;
            }
        }
        clearAndSelect(row, spv.getColumns().get(column));
    }

    /**
     * Clear the current selection and select the cell on the previous row.
     */
    public void clearAndSelectPreviousCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int nextRow = FocusModelListener.getPreviousRowNumber(position, selectionModel.getTableView());
        if (nextRow >= 0) {
            clearAndSelect(nextRow, spv.getColumns().get(position.getColumn()));
        }
    }

    /**
     * Clear the current selection and select the cell on the next row.
     */
    public void clearAndSelectNextCell() {
        TablePosition<ObservableList<SpreadsheetCell>, ?> position = getFocusedCell();
        int nextRow = FocusModelListener.getNextRowNumber(position, selectionModel.getTableView());
        if (nextRow < spv.getProvider().rowCount()) {
            clearAndSelect(nextRow, spv.getColumns().get(position.getColumn()));
        }
    }
}
