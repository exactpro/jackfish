////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IControl;
import com.exactprosystems.jf.api.app.IGuiDictionary;
import com.exactprosystems.jf.api.app.IWindow;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Parameters;

import java.util.Collection;
import java.util.List;

public class ActionGuiHelper
{
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

	public static void extraParameters(List<ReadableValue> list, Matrix matrix, AppConnection connection, String dlgValue, Parameters parameters) throws Exception
	{
		IGuiDictionary dictionary = getGuiDictionary(matrix, connection);
		
		if (dictionary != null)
		{
			IWindow window = dictionary.getWindow(String.valueOf(dlgValue));
			if (window != null)
			{
				Collection<IControl> controls = window.getControls(SectionKind.Run);
				for (IControl control : controls)
				{
					if (!Str.IsNullOrEmpty(control.getID()))
					{
						list.add(new ReadableValue(control.getID(), control.toString()));
					}
				}
			}
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
	
	public static String message(String appId, IWindow window, SectionKind section, IControl control, String msg)
	{
		return "App[" + appId + "] " + "Dialog[" + window.getName() +  "] Section[" + section + "] " 
				+ (control == null ? "" : ("Control[" + control + "] ")) + msg;
	}

}
