






package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.event.EventHandler;
import javafx.scene.control.IndexedCell;
import javafx.scene.control.TablePosition;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Rectangle;

import java.util.List;
import java.util.TreeSet;

public class RectangleSelection extends Rectangle
{
	private final GridViewSkin skin;
	private final TableViewSpanSelectionModel sm;
	private final SelectionRange selectionRange;

	public RectangleSelection(GridViewSkin skin, TableViewSpanSelectionModel sm)
	{
		this.skin = skin;
		this.sm = sm;
		getStyleClass().add("selection-rectangle"); 
		setMouseTransparent(true);

		selectionRange = new SelectionRange();
		skin.getVBar().valueProperty().addListener(layoutListener);
		
		skin.getVBar().addEventFilter(MouseEvent.MOUSE_DRAGGED, new EventHandler<MouseEvent>()
		{
			@Override
			public void handle(MouseEvent event)
			{
				skin.getVBar().valueProperty().removeListener(layoutListener);
				setVisible(false);
				skin.getVBar().addEventFilter(MouseEvent.MOUSE_RELEASED, new EventHandler<MouseEvent>()
				{
					@Override
					public void handle(MouseEvent event)
					{
						skin.getVBar().removeEventFilter(MouseEvent.MOUSE_RELEASED, this);
						skin.getVBar().valueProperty().addListener(layoutListener);
						updateRectangle();
					}
				});
			}
		});

		skin.getHBar().valueProperty().addListener(layoutListener);
		sm.getSelectedCells().addListener((Observable observable) -> {
			skin.getHorizontalHeader().clearSelectedColumns();
			skin.verticalHeader.clearSelectedRows();
			selectionRange.fill(sm.getSelectedCells());
			updateRectangle();
		});
	}

	private final InvalidationListener layoutListener = (Observable observable) -> updateRectangle();

	public GridRange getRange()
	{
		return this.selectionRange.range;
	}

	public final void updateRectangle()
	{
		if (sm.getSelectedCells().isEmpty() || skin.getSelectedRows().isEmpty() || skin.getSelectedColumns().isEmpty() || selectionRange.range == null)
		{
			setVisible(false);
			return;
		}

		IndexedCell topRowCell = skin.getFlow().getTopRow();
		if (topRowCell == null)
		{
			return;
		}
		
		int topRow = topRowCell.getIndex();
		IndexedCell bottomRowCell = skin.getFlow().getCells().get(skin.getFlow().getCells().size() - 1);
		if (bottomRowCell == null)
		{
			return;
		}
		int bottomRow = bottomRowCell.getIndex();

		int minRow = selectionRange.range.getTop();
		if (minRow > bottomRow)
		{
			setVisible(false);
			return;
		}
		minRow = Math.max(minRow, topRow);

		int maxRow = selectionRange.range.getBottom();
		if (maxRow < topRow)
		{
			setVisible(false);
			return;
		}

		maxRow = Math.min(maxRow, bottomRow);
		int minColumn = selectionRange.range.getLeft();
		int maxColumn = selectionRange.range.getRight();

		final CellView call = (CellView) sm.getSelectedCells().get(0).getTableColumn().getCellFactory().call(sm.getSelectedCells().get(0).getTableColumn());

		GridRow gridMinRow = skin.getRowIndexed(minRow);
		if (gridMinRow == null)
		{
			setVisible(false);
			return;
		}

		DataProvider provider = skin.spreadsheetView.getProvider();
		if (maxRow >= provider.rowCount() || maxColumn >= provider.columnCount())
		{
			setVisible(false);
			return;
		}
		handleHorizontalPositioning(minColumn, maxColumn);

		if (getX() + getWidth() < 0)
		{
			setVisible(false);
			return;
		}

		GridRow gridMaxRow = skin.getRowIndexed(maxRow);
		if (gridMaxRow == null)
		{
			setVisible(false);
			return;
		}
		setVisible(true);

		handleVerticalPositioning(minRow, maxRow, gridMinRow, gridMaxRow);
	}

	private void handleVerticalPositioning(int minRow, int maxRow, GridRow gridMinRow, GridRow gridMaxRow)
	{
		double height = 0;
		for (int i = maxRow; i <= maxRow; ++i)
		{
			height += skin.getRowHeight(i);
		}

		yProperty().unbind();
		
		if (gridMinRow.getLayoutY() < skin.getFixedRowHeight())
		{
			setY(skin.getFixedRowHeight());
		}
		else
		{
			yProperty().bind(gridMinRow.layoutYProperty());
		}

		heightProperty().bind(gridMaxRow.layoutYProperty().add(gridMaxRow.verticalShift).subtract(yProperty()).add(height));
	}


	private void handleHorizontalPositioning(int minColumn, int maxColumn)
	{
		double x = 0;

		final List<SpreadsheetColumn> columns = skin.spreadsheetView.getColumns();
		if (columns.size() <= minColumn || columns.size() <= maxColumn)
		{
			return;
		}
		
		for (int i = 0; i < minColumn; ++i)
		{
			
			x += snapSize(columns.get(i).getWidth());
		}

		x -= skin.getHBar().getValue();

		double width = 0;
		for (int i = minColumn; i <= maxColumn /*+ (columnSpan - 1)*/; ++i)
		{
			width += snapSize(columns.get(i).getWidth());
		}

		if (x < skin.fixedColumnWidth)
		{
			
			width -= skin.fixedColumnWidth - x;
			x = skin.fixedColumnWidth;
		}
		setX(x);
		setWidth(width);
	}

	private double snapSize(double value)
	{
		return Math.ceil(value);
	}

	public static class SelectionRange
	{

		private final TreeSet<Long> set = new TreeSet<>();
		private GridRange range;

		public SelectionRange()
		{
		}

		public void fill(List<TablePosition> list)
		{
			set.clear();
			for (TablePosition pos : list)
			{
				set.add(key(pos.getRow(), pos.getColumn()));
			}
			computeRange();
		}

		public GridRange getRange()
		{
			return range;
		}

		private Long key(int row, int column)
		{
			return (((long) row) << 32) | column;
		}

		private int getRow(Long l)
		{
			return (int) (l >> 32);
		}

		private int getColumn(Long l)
		{
			return (int) (l & 0xffFFffFF);
		}

		private void computeRange()
		{
			range = null;
			while (!set.isEmpty())
			{
				if (range != null)
				{
					range = null;
					return;
				}

				long first = set.first();
				set.remove(first);

				int row = getRow(first);
				int column = getColumn(first);

				
				while (set.contains(key(row, column + 1)))
				{
					++column;
					set.remove(key(row, column));
				}

				
				boolean flag = true;
				while (flag)
				{
					++row;
					for (int col = getColumn(first); col <= column; ++col)
					{
						if (!set.contains(key(row, col)))
						{
							flag = false;
							break;
						}
					}
					if (flag)
					{
						for (int col = getColumn(first); col <= column; ++col)
						{
							set.remove(key(row, col));
						}
					}
					else
					{
						--row;
					}
				}
				range = new GridRange(getRow(first), row, getColumn(first), column);
			}
		}
	}

	public static class GridRange
	{

		private final int top;
		private final int bottom;
		private final int left;
		private final int right;

		public GridRange(int top, int bottom, int left, int right)
		{
			this.top = top;
			this.bottom = bottom;
			this.left = left;
			this.right = right;
		}

		public int getTop()
		{
			return top;
		}

		public int getBottom()
		{
			return bottom;
		}

		public int getLeft()
		{
			return left;
		}

		public int getRight()
		{
			return right;
		}

		@Override
		public String toString()
		{
			return "GridRange{" +
					"top=" + top +
					", bottom=" + bottom +
					", left=" + left +
					", right=" + right +
					'}';
		}
	}
}
