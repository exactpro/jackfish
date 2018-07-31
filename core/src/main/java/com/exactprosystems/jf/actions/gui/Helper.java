/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.exceptions.DialogNotFoundException;
import com.exactprosystems.jf.exceptions.app.ApplicationWasClosedException;

import java.util.List;
import java.util.Optional;

class Helper
{
	private Helper()
	{

	}

	public static void dialogsNames(Context context, Matrix matrix, AppConnection connection, List<ReadableValue> list) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		getGuiDictionary(matrix, connection).getWindows().stream()
				.map(IWindow::getName)
				.map(evaluator::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
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
			throw new Exception(R.GUI_HELPER_CHOOSE_DEFAULT_APP.get());
		}
		return dictionary;
	}
	
	public static String message(String appId, IWindow window, SectionKind section, IControl control, Locator locator, String msg)
	{
        return "/" + appId + "/" + window.getName() +  "/" + section + "/" + Str.asString(control) + " - " + Str.asString(locator) + " "
                + msg;
	}

	public static String cutMessage(String msg) {
		return msg.length() > 150 ? msg.substring(0, 150) + " ... See log for more details" : msg;
	}

	/**
	 * @return IWindow from the passed dictionary by passed window name
	 * @throws DialogNotFoundException if dialog not found in the dictionary by passed name
	 */
	public static IWindow getWindow(IGuiDictionary dictionary, String windowName) throws DialogNotFoundException
	{
		return Optional.ofNullable(dictionary.getWindow(windowName)).orElseThrow(() -> new DialogNotFoundException(windowName));
	}

	/**
	 * @return IApplication instance from passed AppConnection, if connection is alive
	 * @throws ApplicationWasClosedException if connection was closed ( via ApplicationStop action)
	 */
	public static IApplication getApplication(AppConnection connection) throws ApplicationWasClosedException
	{
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			throw new ApplicationWasClosedException(id);
		}
		return app;
	}
}
