////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Control;
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

	public void endEdit(boolean commitValue, int column, int row)
	{
		if (commitValue && editing)
		{
			final SpreadsheetView view = handle.getView();
			if (viewCell != null)
			{
				String value = modelCell.getCellType().convertValue(spreadsheetCellEditor.getControlValue());
				// We update the value
				editing = false;
				view.getProvider().setCellValue(modelCell.getColumn(), modelCell.getRow(), value);
				modelCell.setItem(value);
				viewCell.updateItem(modelCell, false);
				viewCell.commitEdit(modelCell);
				end();
				spreadsheetCellEditor.end();

				//We select the cell below if "enter" was typed.
				if (KeyCode.ENTER.equals(lastKeyPressed))
				{
					handle.getView().getSelectionModel().clearAndSelectNextCell(column, row);
				}
				else if (KeyCode.TAB.equals(lastKeyPressed))
				{
					handle.getView().getSelectionModel().clearAndSelectRightCell();
					handle.getCellsViewSkin().scrollHorizontally();
				}
			}
		}
		if (editing)
		{
			editing = false;
			if (viewCell != null)
			{
				viewCell.cancelEdit();
			}
			end();
			if (spreadsheetCellEditor != null)
			{
				spreadsheetCellEditor.end();
			}
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
		//If we do not reset this, it could false the endEdit behavior in case no key was pressed.
		lastKeyPressed = null;
		editing = true;

		handle.getGridView().addEventFilter(KeyEvent.KEY_PRESSED, enterKeyPressed);

		handle.getCellsViewSkin().getVBar().valueProperty().addListener(endEditionListener);
		handle.getCellsViewSkin().getHBar().valueProperty().addListener(endEditionListener);

		Control editor = spreadsheetCellEditor.getEditor();

		// Then we call the user editor in order for it to be ready
		Object value = modelCell.getItem();
		//We don't want the editor to go beyond the cell boundaries
		Double maxHeight = Math.min(handle.getCellsViewSkin().getRowHeight(viewCell.getIndex()), spreadsheetCellEditor.getMaxHeight());

		if (editor != null)
		{
			viewCell.setGraphic(editor);
			editor.setMaxHeight(maxHeight);
			editor.setPrefWidth(viewCell.getWidth());
		}

		spreadsheetCellEditor.startEdit(value);

		if (editor != null)
		{
			focusProperty = getFocusProperty(editor);
			focusProperty.addListener(focusListener);
		}
	}

	private void end()
	{
		if (focusProperty != null)
		{
			focusProperty.removeListener(focusListener);
			focusProperty = null;
		}
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
			if (!isFocus)
			{
				endEdit(true, modelCell.getColumn(), modelCell.getRow());
			}
		}
	};

	private final InvalidationListener endEditionListener = new InvalidationListener()
	{
		@Override
		public void invalidated(Observable observable)
		{
			endEdit(true, modelCell.getColumn(), modelCell.getRow());
		}
	};
}
