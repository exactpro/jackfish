////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.app.Locator;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.exceptions.DialogNotFoundException;

import java.util.List;

class Helper
{
	private Helper()
	{

	}

	public static void dialogsNames(Context context, Matrix matrix, AppConnection connection, List<ReadableValue> list) throws Exception
	{
		IGuiDictionary dictionary = getGuiDictionary(matrix, connection);

		AbstractEvaluator evaluator = context.getEvaluator();
		for (IWindow window : dictionary.getWindows())
		{
			String quoted = evaluator.createString(window.getName());
			list.add(new ReadableValue(quoted));
		}
	}

	public static void extraParameters(List<ReadableValue> list, Matrix matrix, AppConnection connection, String dlgValue, boolean needQuote) throws Exception
	{
		IGuiDictionary dictionary = getGuiDictionary(matrix, connection);
		
		IWindow window = dictionary.getWindow(String.valueOf(dlgValue));
		if (window != null)
		{
			window.getControls(SectionKind.Run)
					.stream()
					.filter(control -> !Str.IsNullOrEmpty(control.getID()))
					.map(control -> {
						String id = control.getID();
						if (needQuote)
						{
							id = "'" + id + "'";
						}
						return new ReadableValue(id, control.toString());
					})
					.forEach(list::add);
		}
	}

	public static IGuiDictionary getGuiDictionary(Matrix matrix, AppConnection connection) throws Exception
	{
		IGuiDictionary dictionary = null;
		if (connection != null)
		{
			 dictionary = connection.getDictionary();
		}
		if (dictionary == null)
		{
			dictionary = matrix.getDefaultApp() == null ? null : matrix.getDefaultApp().getDictionary();
		}

		if (dictionary == null)
		{
			throw new Exception("You need to set up default application");
		}
		return dictionary;
	}
	
	public static String message(String appId, IWindow window, SectionKind section, IControl control, Locator locator, String msg)
	{
        return "/" + appId + "/" + window.getName() +  "/" + section + "/" + Str.asString(control) + " - " + Str.asString(locator) + " "
                + msg;
	}

	public static void throwExceptionIfDialogNull(IWindow window, String windowName) throws DialogNotFoundException
	{
		if (window == null)
		{
			throw new DialogNotFoundException(windowName);
		}
	}

}
