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
