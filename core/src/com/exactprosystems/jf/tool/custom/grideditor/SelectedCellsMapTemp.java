////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.scene.control.TablePositionBase;

import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class SelectedCellsMapTemp<T extends TablePositionBase>
{
	private final ObservableList<T> selectedCells;
	private final ObservableList<T> sortedSelectedCells;

	private final Map<Integer, BitSet> selectedCellBitSetMap;

	public SelectedCellsMapTemp(final ListChangeListener<T> listener)
	{
		selectedCells = FXCollections.<T>observableArrayList();
		sortedSelectedCells = new SortedList<>(selectedCells, (T o1, T o2) -> {
			int result = o1.getRow() - o2.getRow();
			return result == 0 ? (o1.getColumn() - o2.getColumn()) : result;
		});
		sortedSelectedCells.addListener(listener);

		selectedCellBitSetMap = new TreeMap<>(Integer::compareTo);
	}

	public int size()
	{
		return selectedCells.size();
	}

	public T get(int i)
	{
		if (i < 0 || i > this.sortedSelectedCells.size())
		{
			return null;
		}
		return sortedSelectedCells.get(i);
	}

	public void add(T tp)
	{
		final int row = tp.getRow();
		final int columnIndex = tp.getColumn();

		BitSet bitset;
		if (!selectedCellBitSetMap.containsKey(row))
		{
			bitset = new BitSet();
			selectedCellBitSetMap.put(row, bitset);
		}
		else
		{
			bitset = selectedCellBitSetMap.get(row);
		}

		if (columnIndex >= 0)
		{
			boolean isAlreadySet = bitset.get(columnIndex);
			bitset.set(columnIndex);

			if (!isAlreadySet)
			{
				selectedCells.add(tp);
			}
		}
		else
		{
			if (!selectedCells.contains(tp))
			{
				selectedCells.add(tp);
			}
		}
	}

	public void addAll(Collection<T> cells)
	{
		for (T tp : cells)
		{
			final int row = tp.getRow();
			final int columnIndex = tp.getColumn();

			BitSet bitset;
			if (!selectedCellBitSetMap.containsKey(row))
			{
				bitset = new BitSet();
				selectedCellBitSetMap.put(row, bitset);
			}
			else
			{
				bitset = selectedCellBitSetMap.get(row);
			}

			if (columnIndex < 0)
			{
				continue;
			}

			bitset.set(columnIndex);
		}


		selectedCells.addAll(cells);
	}

	public void setAll(Collection<T> cells)
	{

		selectedCellBitSetMap.clear();
		for (T tp : cells)
		{
			final int row = tp.getRow();
			final int columnIndex = tp.getColumn();


			BitSet bitset;
			if (!selectedCellBitSetMap.containsKey(row))
			{
				bitset = new BitSet();
				selectedCellBitSetMap.put(row, bitset);
			}
			else
			{
				bitset = selectedCellBitSetMap.get(row);
			}

			if (columnIndex < 0)
			{
				continue;
			}

			bitset.set(columnIndex);
		}


		selectedCells.setAll(cells);
	}

	public void remove(T tp)
	{
		final int row = tp.getRow();
		final int columnIndex = tp.getColumn();


		if (selectedCellBitSetMap.containsKey(row))
		{
			BitSet bitset = selectedCellBitSetMap.get(row);

			if (columnIndex >= 0)
			{
				bitset.clear(columnIndex);
			}

			if (bitset.isEmpty())
			{
				selectedCellBitSetMap.remove(row);
			}
		}


		selectedCells.remove(tp);
	}

	public void clear()
	{

		selectedCellBitSetMap.clear();


		selectedCells.clear();
	}

	public boolean isSelected(int row, int columnIndex)
	{
		if (columnIndex < 0)
		{
			return selectedCellBitSetMap.containsKey(row);
		}
		else
		{
			return selectedCellBitSetMap.containsKey(row) ? selectedCellBitSetMap.get(row).get(columnIndex) : false;
		}
	}

	public int indexOf(T tp)
	{
		return sortedSelectedCells.indexOf(tp);
	}

	public boolean isEmpty()
	{
		return selectedCells.isEmpty();
	}

	public ObservableList<T> getSelectedCells()
	{
		return selectedCells;
	}
}
