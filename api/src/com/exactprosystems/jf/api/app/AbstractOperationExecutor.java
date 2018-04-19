/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.client.ICondition;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Proxy class for all OperationExecutors. <br>
 * This class need contains all methods, which return strings value and trimmed them, if needed <br>
 */
public abstract class AbstractOperationExecutor<T> implements OperationExecutor<T>
{
	private boolean useTrimText = true;
	protected PluginInfo info;

	public AbstractOperationExecutor(boolean useTrimText)
	{
		this.useTrimText = useTrimText;
	}

	@Override
	public void setPluginInfo(PluginInfo info)
	{
		this.info = info;
	}

	@Override
	public boolean isAllowed(ControlKind kind, OperationKind operation)
	{
		return this.info.isAllowed(kind, operation);
	}

	@Override
	public boolean isSupported(ControlKind kind)
	{
		return this.info.isSupported(kind);
	}

	@Override
	public String getValue(T component) throws Exception
	{
		return this.trimIfNeeded(this.getValueDerived(component));
	}

	protected abstract String getValueDerived(T component) throws Exception;

	@Override
	public List<String> getList(T component, boolean onlyVisible) throws Exception
	{
		List<String> listDerived = this.getListDerived(component, onlyVisible);
		List<String> trimmedList = new ArrayList<>(listDerived.size());
		for (String s : listDerived)
		{
			trimmedList.add(this.trimIfNeeded(s));
		}
		return trimmedList;
	}

	protected abstract List<String> getListDerived(T component, boolean onlyVisible) throws Exception;

	@Override
	public String get(T component) throws Exception
	{
		return this.trimIfNeeded(this.getDerived(component));
	}

	protected abstract String getDerived(T component) throws Exception;

	@Override
	public String getAttr(T component, String name) throws Exception
	{
		return this.trimIfNeeded(this.getAttrDerived(component, name));
	}

	protected abstract String getAttrDerived(T component, String name) throws Exception;

	@Override
	public String script(T component, String script) throws Exception
	{
		return this.trimIfNeeded(this.scriptDerived(component, script));
	}

	protected abstract String scriptDerived(T component, String script) throws Exception;

	@Override
	public String getValueTableCell(T component, int column, int row) throws Exception
	{
		return this.trimIfNeeded(this.getValueTableCellDerived(component, column, row));
	}

	protected abstract String getValueTableCellDerived(T component, int column, int row) throws Exception;

	@Override
	public Map<String, String> getRow(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception
	{
		Map<String, String> rowDerived = this.getRowDerived(component, additional, header, useNumericHeader, columns, valueCondition, colorCondition);
		Map<String, String> trimmedMap = new LinkedHashMap<>(rowDerived.size());
		for (Map.Entry<String, String> entry : rowDerived.entrySet())
		{
			trimmedMap.put(this.trimIfNeeded(entry.getKey()), this.trimIfNeeded(entry.getValue()));
		}
		return trimmedMap;
	}

	protected abstract Map<String, String> getRowDerived(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, ICondition valueCondition, ICondition colorCondition) throws Exception;

	@Override
	public Map<String, String> getRowByIndex(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		Map<String, String> row = this.getRowByIndexDerived(component, additional, header, useNumericHeader, columns, i);
		Map<String, String> trimmedMap = new LinkedHashMap<>(row.size());
		for (Map.Entry<String, String> entry : row.entrySet())
		{
			trimmedMap.put(this.trimIfNeeded(entry.getKey()), this.trimIfNeeded(entry.getValue()));
		}
		return trimmedMap;
	}

	protected abstract Map<String, String> getRowByIndexDerived(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception;

	@Override
	public Map<String, ValueAndColor> getRowWithColor(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception
	{
		Map<String, ValueAndColor> row = this.getRowWithColorDerived(component, additional, header, useNumericHeader, columns, i);
		Map<String, ValueAndColor> trimmedMap = new LinkedHashMap<>();

		for (Map.Entry<String, ValueAndColor> entry : row.entrySet())
		{
			trimmedMap.put(
					this.trimIfNeeded(entry.getKey())
					, new ValueAndColor(this.trimIfNeeded(entry.getValue().getValue()), entry.getValue().getColor(), entry.getValue().getBackColor())
			);
		}
		return trimmedMap;
	}

	protected abstract Map<String, ValueAndColor> getRowWithColorDerived(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns, int i) throws Exception;

	@Override
	public String[][] getTable(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception
	{
		String[][] table = getTableDerived(component, additional, header, useNumericHeader, columns);
		String[][] trimmedTable = new String[table.length][];
		for (int i = 0; i < table.length; i++)
		{
			trimmedTable[i] = new String[table[i].length];
			for (int j = 0; j < table[i].length; j++)
			{
				trimmedTable[i][j] = this.trimIfNeeded(table[i][j]);
			}
		}
		return trimmedTable;
	}

	protected abstract String[][] getTableDerived(T component, Locator additional, Locator header, boolean useNumericHeader, String[] columns) throws Exception;

	private String trimIfNeeded(String s)
	{
		if (s != null && this.useTrimText)
		{
			return s.trim();
		}
		return s;
	}
}
