////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
import javafx.scene.control.TablePosition;
import javafx.scene.control.TextArea;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public class GridCellEditor
{
	private final SpreadsheetHandle handle;
	private SpreadsheetCell modelCell;
	private CellView viewCell;
	private BooleanExpression focusProperty;

	private boolean editing = false;
	private SpreadsheetCellEditor spreadsheetCellEditor;
	private KeyCode lastKeyPressed;

	public GridCellEditor(SpreadsheetHandle handle)
	{
		this.handle = handle;
	}

	public void updateDataCell(SpreadsheetCell cell)
	{
		this.modelCell = cell;
	}

	public void updateSpreadsheetCell(CellView cell)
	{
		this.viewCell = cell;
	}

	public void updateSpreadsheetCellEditor(final SpreadsheetCellEditor spreadsheetCellEditor)
	{
		this.spreadsheetCellEditor = spreadsheetCellEditor;
	}

	public void endEdit(boolean commitValue)
	{
		if (commitValue && editing)
		{
			final SpreadsheetView view = handle.getView();
			if (viewCell != null)
			{
				String value = modelCell.getCellType().convertValue(spreadsheetCellEditor.getControlValue());

				view.getProvider().setCellValue(modelCell.getColumn(), modelCell.getRow(), value);
				editing = false;
 				viewCell.commitEdit(modelCell);
				end();
				spreadsheetCellEditor.end();


				view.getCellsViewSkin().resizeRowsToFitContent();

				if(lastKeyPressed == KeyCode.ENTER){
					TablePosition<ObservableList<SpreadsheetCell>, ?> position = (TablePosition<ObservableList<SpreadsheetCell>, ?>) handle.getGridView().
							getFocusModel().getFocusedCell();
					if (position != null) {
						int nextRow = FocusModelListener.getNextRowNumber(position, handle.getGridView());
						if(nextRow < handle.getView().getProvider().rowCount()){
							handle.getGridView().getSelectionModel().clearAndSelect(nextRow, position.getTableColumn());
						}
					}
				}
			}
		}

		if (viewCell != null && editing) {
			editing = false;
			viewCell.cancelEdit();
			end();
			spreadsheetCellEditor.end();
		}
	}

	/**
	 * Return if this editor is currently being used.
	 *
	 * @return if this editor is being used.
	 */
	public boolean isEditing()
	{
		return editing;
	}

	public SpreadsheetCell getModelCell()
	{
		return modelCell;
	}

	/***************************************************************************
	 * * Protected/Private Methods * *
	 **************************************************************************/
	void startEdit()
	{
		editing = true;

		handle.getGridView().addEventFilter(KeyEvent.KEY_PRESSED, enterKeyPressed);

		handle.getCellsViewSkin().getVBar().valueProperty().addListener(endEditionListener);
		handle.getCellsViewSkin().getHBar().valueProperty().addListener(endEditionListener);

		viewCell.setGraphic(spreadsheetCellEditor.getEditor());

		Object value = modelCell.getItem();
		Double maxHeight = Math.max(handle.getCellsViewSkin().getRowHeight(viewCell.getIndex()), spreadsheetCellEditor.getMaxHeight());
		spreadsheetCellEditor.getEditor().setMaxHeight(maxHeight);
		spreadsheetCellEditor.getEditor().setPrefWidth(viewCell.getWidth());

		if(handle.getGridView().getEditWithKey()){
			handle.getGridView().setEditWithKey(false);
			spreadsheetCellEditor.startEdit("");
		}else{
			spreadsheetCellEditor.startEdit(value);
		}

		focusProperty = getFocusProperty(spreadsheetCellEditor.getEditor());

		focusProperty.addListener(focusListener);
	}

	private void end()
	{
		focusProperty.removeListener(focusListener);
		focusProperty = null;
		handle.getCellsViewSkin().getVBar().valueProperty().removeListener(endEditionListener);
		handle.getCellsViewSkin().getHBar().valueProperty().removeListener(endEditionListener);

		handle.getGridView().removeEventFilter(KeyEvent.KEY_PRESSED, enterKeyPressed);

		this.modelCell = null;
		this.viewCell = null;
	}

	/**
	 * If we have a TextArea, we need to return a custom BooleanExpression
	 * because we want to let the editor in place even if the user is touching
	 * the scrollBars inside the textArea.
	 *
	 * @param control
	 * @return
	 */
	private BooleanExpression getFocusProperty(Control control)
	{
		if (control instanceof TextArea)
		{
			return Bindings.createBooleanBinding(() -> {
				if (handle.getView().getScene() == null)
				{
					return false;
				}
				for (Node n = handle.getView().getScene().getFocusOwner(); n != null; n = n.getParent())
				{
					if (n == control)
					{
						return true;
					}
				}
				return false;
			}, handle.getView().getScene().focusOwnerProperty());
		}
		else
		{
			return control.focusedProperty();
		}
	}

	/**
	 * When we stop editing a cell, if enter was pressed, we want to go to the next line.
	 */
	private final EventHandler<KeyEvent> enterKeyPressed = new EventHandler<KeyEvent>()
	{
		@Override
		public void handle(KeyEvent t)
		{
			lastKeyPressed = t.getCode();
		}
	};

	private final ChangeListener<Boolean> focusListener = new ChangeListener<Boolean>()
	{
		@Override
		public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean isFocus)
		{
			if (!isFocus && lastKeyPressed != KeyCode.ENTER)
			{
				endEdit(true);
			}
		}
	};

	private final InvalidationListener endEditionListener = observable -> endEdit(true);
}
