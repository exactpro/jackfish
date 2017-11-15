////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.functions.Table;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TableUtils
{
	private TableUtils()
	{}

	public static boolean tableIsValid(Table table, String dialogName, IGuiDictionary dictionary, Function<String, Boolean> function)
	{
		if (table.isEmptyTable())
		{
			return true;
		}
		boolean[] hasError = {false};
		StringBuilder sbError = new StringBuilder("The table is not valid. See errors below :\n");
		int[] errorCount = {0};
		Optional<String> first = dictionary.getWindows().stream().map(IWindow::getName).filter(dialogName::equals).findFirst();
		if (!first.isPresent())
		{
			addError(String.format("In the header with number 0 need be the name of dialog. Dialog with name '%s' not found", dialogName), hasError, errorCount, sbError);
		}
		if (table.getHeaderSize() - 1 != table.size())
		{
			addError("The table must be square", hasError, errorCount, sbError);
		}
		IntStream.range(0, Math.min(table.getHeaderSize() - 1, table.size())).forEach(i ->
		{
			String header = table.getHeader(i + 1);
			String cell = String.valueOf(table.get(i).get(dialogName));
			if (!header.equals(cell))
			{
				addError(String.format("The row with index %d ( %s ) not equals the header with index %d ( %s) ", i, cell, i + 1, header), hasError, errorCount, sbError);
			}
		});
		boolean result = !hasError[0] || function.apply(sbError.toString());
		if (result && hasError[0])
		{
			table.clear();
			table.removeColumns(IntStream.range(0, table.getHeaderSize())
					.mapToObj(table::getHeader)
					.collect(Collectors.toList())
					.toArray(new String[table.getHeaderSize()])
			);
		}
		return result;
	}

	private static void addError(String msg, boolean[] hasError, int[] errorCount, StringBuilder sb)
	{
		sb.append(errorCount[0]++).append(": ");
		sb.append(msg).append("\n");
		hasError[0] = true;
	}
}
