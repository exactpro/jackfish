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
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.collections.ObservableList;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.NodeOrientation;
import javafx.scene.Cursor;
import javafx.scene.control.*;
import javafx.scene.control.TableView.TableViewSelectionModel;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Stack;
import java.util.stream.Collectors;

public class VerticalHeader extends StackPane
{

	public static final int PICKER_SIZE = 16;
	private static final int DRAG_RECT_HEIGHT = 5;
	private static final String TABLE_ROW_KEY = "TableRow";
	private static final String PICKER_INDEX = "PickerIndex";
	private static final String TABLE_LABEL_KEY = "Label";

	private final SpreadsheetHandle handle;
	private final SpreadsheetView spreadsheetView;
	private double horizontalHeaderHeight;

	private final DoubleProperty innerVerticalHeaderWidth = new SimpleDoubleProperty();
	private Rectangle clip; // Ensure that children do not go out of bounds
	private ContextMenu blankContextMenu;

	// used for column resizing
	private double lastY = 0.0F;
	private static double dragAnchorY = 0.0;

	// drag rectangle overlays
	private final List<Rectangle> dragRects = new ArrayList<>();

	private final List<Label> labelList = new ArrayList<>();
	private GridViewSkin skin;
	private boolean resizing = false;

	private final Stack<Label> pickerPile;
	private final Stack<Label> pickerUsed;


	private final BitSet selectedRows = new BitSet();

	public VerticalHeader(final SpreadsheetHandle handle)
	{
		this.handle = handle;
		this.spreadsheetView = handle.getView();
		pickerPile = new Stack<>();
		pickerUsed = new Stack<>();
	}

	void init(final GridViewSkin skin, HorizontalHeader horizontalHeader)
	{
		this.skin = skin;
		horizontalHeader.heightProperty().addListener((arg0, oldHeight, newHeight) -> {
			horizontalHeaderHeight = newHeight.doubleValue();
			requestLayout();
		});

		handle.getView().providerProperty().addListener(layout);

		clip = new Rectangle(getVerticalHeaderWidth(), snapSize(skin.getSkinnable().getHeight()));
		clip.relocate(snappedTopInset(), snappedLeftInset());
		clip.setSmooth(false);
		clip.heightProperty().bind(skin.getSkinnable().heightProperty());
		clip.widthProperty().bind(innerVerticalHeaderWidth);
		VerticalHeader.this.setClip(clip);

		spreadsheetView.rowHeaderWidthProperty().addListener(layout);

		spreadsheetView.heightProperty().addListener(layout);

		spreadsheetView.getRowPickers().addListener(layout);

		skin.getSelectedRows().addListener(layout);

		blankContextMenu = new ContextMenu();
	}

	public double getVerticalHeaderWidth()
	{
		return innerVerticalHeaderWidth.get();
	}

	public ReadOnlyDoubleProperty verticalHeaderWidthProperty()
	{
		return innerVerticalHeaderWidth;
	}

	public double computeHeaderWidth()
	{
		double width = 0;
		if (!spreadsheetView.getRowPickers().isEmpty())
		{
			width += PICKER_SIZE;
		}
		width += spreadsheetView.getRowHeaderWidth();
		return width;
	}

	void clearSelectedRows()
	{
		selectedRows.clear();
	}

	@Override
	protected void layoutChildren()
	{
		if (resizing)
		{
			return;
		}
		if (skin.getCellsSize() > 0)
		{

			double x = snappedLeftInset();
			pickerPile.addAll(pickerUsed.subList(0, pickerUsed.size()));
			pickerUsed.clear();
			if (!spreadsheetView.getRowPickers().isEmpty())
			{
				innerVerticalHeaderWidth.setValue(PICKER_SIZE);
				x += PICKER_SIZE;
			}
			else
			{
				innerVerticalHeaderWidth.setValue(0);
			}
			innerVerticalHeaderWidth.setValue(getVerticalHeaderWidth() + spreadsheetView.getRowHeaderWidth());

			getChildren().clear();

			final int cellSize = skin.getCellsSize();

			int rowCount = 0;
			Label label;

			rowCount = addVisibleRows(rowCount, x, cellSize);

			label = getLabel(rowCount++, null);
			label.setOnMousePressed((MouseEvent event) -> {
				spreadsheetView.getSelectionModel().selectAll();
			});
			label.setText(""); //$NON-NLS-1$
			label.resize(spreadsheetView.getRowHeaderWidth(), horizontalHeaderHeight);
			label.layoutYProperty().unbind();
			label.setLayoutY(0);
			label.setLayoutX(x);
			label.getStyleClass().clear();
			label.setContextMenu(blankContextMenu);
			getChildren().add(label);

			ScrollBar hbar = handle.getCellsViewSkin().getHBar();
			if (hbar.isVisible())
			{
				// Last one blank and on top (z-order) of the others
				label = getLabel(rowCount++, null);
				label.getProperties().put(TABLE_ROW_KEY, null);
				label.setText(""); //$NON-NLS-1$
				label.resize(getVerticalHeaderWidth(), hbar.getHeight());
				label.layoutYProperty().unbind();
				label.relocate(snappedLeftInset(), getHeight() - hbar.getHeight());
				label.getStyleClass().clear();
				label.setContextMenu(blankContextMenu);
				getChildren().add(label);
			}
		}
		else
		{
			getChildren().clear();
		}
	}

	private int addVisibleRows(int rowCount, double x, int cellSize)
	{
		int rowIndex;
		double y = snappedTopInset();
		y += horizontalHeaderHeight;

		if (cellSize != 0)
		{
			y += skin.getRow(0).getLocalToParentTransform().getTy();
		}

		Label label;
		final int modelRowCount = spreadsheetView.getProvider().rowCount();

		int i = 0;

		GridRow row = skin.getRow(i);

		double fixedRowHeight = skin.getFixedRowHeight();
		double rowHeaderWidth = spreadsheetView.getRowHeaderWidth();
		double height;

		// We iterate over the visibleRows
		while (cellSize != 0 && row != null && row.getIndex() < modelRowCount)
		{
			rowIndex = row.getIndex();
			height = row.getHeight();
			if (row.getLayoutY() >= fixedRowHeight && spreadsheetView.getRowPickers().containsKey(rowIndex))
			{
				Label picker = getPicker(spreadsheetView.getRowPickers().get(rowIndex));
				picker.resize(PICKER_SIZE, height);
				picker.layoutYProperty().bind(row.layoutYProperty().add(horizontalHeaderHeight));
				getChildren().add(picker);
			}

			label = getLabel(rowCount++, rowIndex);
			label.getProperties().put(TABLE_ROW_KEY, row);
			label.setText(getRowHeader(rowIndex));
			label.resize(rowHeaderWidth, height);
			label.setLayoutX(x);
			label.layoutYProperty().bind(row.layoutYProperty().add(horizontalHeaderHeight));
			label.setContextMenu(getRowContextMenu(rowIndex));

			getChildren().add(label);
			final ObservableList<String> css = label.getStyleClass();
			if (skin.getSelectedRows().contains(rowIndex))
			{
				css.addAll("selected"); //$NON-NLS-1$
			}
			else
			{
				css.removeAll("selected"); //$NON-NLS-1$
			}
			css.removeAll("fixed"); //$NON-NLS-1$

			y += height;

			Rectangle dragRect = getDragRect(rowCount++);
			dragRect.getProperties().put(TABLE_ROW_KEY, row);
			dragRect.getProperties().put(TABLE_LABEL_KEY, label);
			dragRect.setWidth(label.getWidth());
			dragRect.relocate(snappedLeftInset() + x, y - DRAG_RECT_HEIGHT);
			getChildren().add(dragRect);
			row = skin.getRow(++i);
		}
		return rowCount;
	}

	private final EventHandler<MouseEvent> rectMousePressed = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent me)
		{

			if (me.getClickCount() == 2 && me.isPrimaryButtonDown())
			{
				Rectangle rect = (Rectangle) me.getSource();
				GridRow row = (GridRow) rect.getProperties().get(TABLE_ROW_KEY);
				skin.resizeRowToFitContent(row.getIndex());
				requestLayout();
			}
			else
			{
				dragAnchorY = me.getSceneY();
				resizing = true;
			}
			me.consume();
		}
	};

	private final EventHandler<MouseEvent> rectMouseDragged = me -> {
		Rectangle rect = (Rectangle) me.getSource();
		GridRow row = (GridRow) rect.getProperties().get(TABLE_ROW_KEY);
		Label label = (Label) rect.getProperties().get(TABLE_LABEL_KEY);
		if (row != null)
		{
			rowResizing(row, label, me);
		}
		me.consume();
	};

	private void rowResizing(GridRow gridRow, Label label, MouseEvent me)
	{
		double draggedY = me.getSceneY() - dragAnchorY;
		if (gridRow.getEffectiveNodeOrientation() == NodeOrientation.RIGHT_TO_LEFT)
		{
			draggedY = -draggedY;
		}

		double delta = draggedY - lastY;

		Double newHeight = gridRow.getHeight() + delta;
		if (newHeight < 0)
		{
			return;
		}
		handle.getCellsViewSkin().rowHeightMap.put(gridRow.getIndex(), newHeight);
		Event.fireEvent(spreadsheetView, new SpreadsheetView.RowHeightEvent(gridRow.getIndex(), newHeight));
		label.resize(spreadsheetView.getRowHeaderWidth(), newHeight);
		gridRow.setPrefHeight(newHeight);
		gridRow.requestLayout();

		lastY = draggedY;
	}

	private final EventHandler<MouseEvent> rectMouseReleased = new EventHandler<MouseEvent>()
	{
		@Override
		public void handle(MouseEvent me)
		{
			lastY = 0.0F;
			resizing = false;
			requestLayout();
			me.consume();
			Rectangle rect = (Rectangle) me.getSource();
			GridRow row = (GridRow) rect.getProperties().get(TABLE_ROW_KEY);
			if (selectedRows.get(row.getIndex()))
			{
				double height = row.getHeight();
				for (int i = selectedRows.nextSetBit(0); i >= 0; i = selectedRows.nextSetBit(i + 1))
				{
					skin.rowHeightMap.put(i, height);
					Event.fireEvent(spreadsheetView, new SpreadsheetView.RowHeightEvent(i, height));
				}
			}
		}
	};

	private Label getLabel(int rowNumber, Integer row)
	{
		Label label;
		if (labelList.isEmpty() || labelList.size() <= rowNumber)
		{
			label = new Label();
			labelList.add(label);
		}
		else
		{
			label = labelList.get(rowNumber);
		}
		// We want to select the whole row when clicking on a header.
		label.setOnMousePressed(row == null ? null : (MouseEvent event) -> {
			if (event.isPrimaryButtonDown())
			{
				if (event.getClickCount() == 2)
				{
					skin.resizeRowToFitContent(row);
					requestLayout();
				}
				else
				{
					headerClicked(row, event);
				}
			}
		});
		return label;
	}

	private void headerClicked(int row, MouseEvent event)
	{
		TableViewSelectionModel<ObservableList<SpreadsheetCell>> sm = handle.getGridView().getSelectionModel();
		int focusedRow = sm.getFocusedIndex();
		int rowCount = handle.getView().getProvider().rowCount();
		ObservableList<TableColumn<ObservableList<SpreadsheetCell>, ?>> columns = sm.getTableView().getColumns();
		if (columns.size() > 0)
		{
			TableColumn<ObservableList<SpreadsheetCell>, ?> firstColumn = columns.get(0);
			TableColumn<ObservableList<SpreadsheetCell>, ?> lastColumn = columns.get(columns.size() - 1);

			if (event.isShortcutDown())
			{
				BitSet tempSet = (BitSet) selectedRows.clone();
				sm.selectRange(row, firstColumn, row, lastColumn);
				selectedRows.or(tempSet);
				selectedRows.set(row);
			}
			else if (event.isShiftDown() && focusedRow >= 0 && focusedRow < rowCount)
			{
				sm.clearSelection();
				sm.selectRange(focusedRow, firstColumn, row, lastColumn);
				//We want to let the focus on the focused row.
				sm.getTableView().getFocusModel().focus(focusedRow, firstColumn);
				int min = Math.min(row, focusedRow);
				int max = Math.max(row, focusedRow);
				selectedRows.set(min, max + 1);
			}
			else
			{
				sm.clearSelection();
				sm.selectRange(row, firstColumn, row, lastColumn);
				//And we want to have the focus on the first cell in order to be able to copy/paste between rows.
				sm.getTableView().getFocusModel().focus(row, firstColumn);
				selectedRows.set(row);
			}
		}
	}

	private Label getPicker(Picker picker)
	{
		Label pickerLabel;
		if (pickerPile.isEmpty())
		{
			pickerLabel = new Label();
			picker.getStyleClass().addListener(layout);
			pickerLabel.setOnMouseClicked(pickerMouseEvent);
		}
		else
		{
			pickerLabel = pickerPile.pop();
		}
		pickerUsed.push(pickerLabel);

		pickerLabel.getStyleClass().setAll(picker.getStyleClass());
		pickerLabel.getProperties().put(PICKER_INDEX, picker);
		return pickerLabel;
	}

	private final EventHandler<MouseEvent> pickerMouseEvent = mouseEvent -> {
		Label picker = (Label) mouseEvent.getSource();
		((Picker) picker.getProperties().get(PICKER_INDEX)).onClick();
	};

	private Rectangle getDragRect(int rowNumber)
	{
		if (dragRects.isEmpty() || dragRects.size() <= rowNumber)
		{
			final Rectangle rect = new Rectangle();
			rect.setWidth(getVerticalHeaderWidth());
			rect.setHeight(DRAG_RECT_HEIGHT);
			rect.setFill(Color.TRANSPARENT);
			rect.setSmooth(false);
			rect.setOnMousePressed(rectMousePressed);
			rect.setOnMouseDragged(rectMouseDragged);
			rect.setOnMouseReleased(rectMouseReleased);
			rect.setCursor(Cursor.V_RESIZE);
			dragRects.add(rect);
			return rect;
		}
		else
		{
			return dragRects.get(rowNumber);
		}
	}

	private ContextMenu getRowContextMenu(final Integer row)
	{
		final ContextMenu contextMenu = new ContextMenu();
		contextMenu.setAutoHide(true);

		MenuItem addRowBefore = new MenuItem("Add before row " + row);
		addRowBefore.setOnAction(e -> this.spreadsheetView.addRowBefore(row));

		MenuItem addRowAfter = new MenuItem("Add after row " + row);
		addRowAfter.setOnAction(e -> this.spreadsheetView.addRowAfter(row));

		MenuItem moveUpRow = new MenuItem("Move up this row");
		moveUpRow.setOnAction(e -> this.spreadsheetView.swapRows(row, row - 1));

		MenuItem moveDownRow = new MenuItem("Move down this row");
		moveDownRow.setOnAction(e -> this.spreadsheetView.swapRows(row, row + 1));

		MenuItem removeRow = new MenuItem("Remove rows");
		removeRow.setOnAction(e -> this.spreadsheetView.removeRows(this.spreadsheetView.getSelectionModel().getSelectedCells().stream().map(TablePositionBase::getRow).distinct().collect(Collectors.toList())));

		contextMenu.getItems().addAll(addRowBefore, addRowAfter, moveUpRow, moveDownRow, removeRow);
		return contextMenu;
	}

	private String getRowHeader(int index)
	{
		return spreadsheetView.getProvider().getRowHeaders().size() > index ? (String) spreadsheetView.getProvider().getRowHeaders().get(index) : String.valueOf(index + 1);
	}

	private final InvalidationListener layout = (Observable arg0) -> {
		requestLayout();
	};
}
