/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/
package com.exactprosystems.jf.tool.custom.grideditor;

import javafx.util.StringConverter;
import javafx.util.converter.DefaultStringConverter;

public class StringCellType
{
	private StringConverter<String> converter;

	public StringCellType()
	{
		this(new DefaultStringConverter());
	}

	public StringCellType(StringConverter<String> converter)
	{
		this.converter = converter;
	}

	public static SpreadsheetCell createCell(final int row, final int column, final String value)
	{
		StringCellType type = new StringCellType();
		SpreadsheetCell cell = new SpreadsheetCellBase(row, column, type);
		cell.setItem(value);
		return cell;
	}

	public SpreadsheetCellEditor createEditor(SpreadsheetView view)
	{
		return new SpreadsheetCellEditor.StringEditor(view);
	}

	public String convertValue(Object value)
	{
		String convertedValue = converter.fromString(value == null ? null : value.toString());
		if (convertedValue == null || convertedValue.equals(""))
		{
			return "";
		}
		return convertedValue;
	}

	public String toString(String item)
	{
		return converter.toString(item);
	}

}
