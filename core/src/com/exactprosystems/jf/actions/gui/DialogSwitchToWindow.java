////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2016, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.gui;

import static com.exactprosystems.jf.actions.gui.Helper.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group 					= ActionGroups.GUI,
		generalDescription 		= "The purpose of this action is to switch focus to another application.\n"
				+ "Plug-in dependent action currently being used only with web plug-in.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Start the web application.`}}"
				+ "{{`2. Connect to the desktop application, the window headed WinApp.`}}"
				+ "{{`3. Switch focus back to the MainFrame of the web application, located in MyDialog.`}}"
				+ "{{#\n" +
				"#Id;#Action;#Browser;#URL;#AppId\n"
				+ "APPSTR1;ApplicationStart;'Chrome';'https://google.com';'WEB'\n"
				+ "#Id;#Action;#AppId;#Main window\n"
				+ "APPSTR2;ApplicationConnectTo;'WIN';'WinApp'\n"
				+ "#Action;#Frame;#Dialog;#AppConnection\n"
				+ "DialogSwitchToWindow;'MainFrame';'MyDialog';APPSTR1.Out#}}"
	)
public class DialogSwitchToWindow extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";
	public static final String	dialogName		= "Dialog";
	public static final String frameName = "Frame";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, def = DefaultValuePool.Null, description = "The name of the dialog containing the frame which needs to be connected to."
			+ "If is absent tool will switch to the parent frame.")
	protected String			dialog;

	@ActionFieldAttribute(name = frameName, mandatory = false, def = DefaultValuePool.Null, description = "Frame name to which the focus will be set.")
	protected String 			frame;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case dialogName:
				return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (connection == null)
		{
			throw new NullPointerException(String.format("Field with name '%s' can't be null", connectionName));
		}
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			throw new NullPointerException(String.format("Service with id '%s' not started yet", id));
		}
		
		if (this.dialog == null)
		{
			service.switchToFrame(null);
		}
		else
		{
			if (this.frame == null)
			{
				throw new Exception("Parameter 'frame' is needed for not-null 'dialog' parameter.");
			}
			IGuiDictionary dictionary = this.connection.getDictionary();
			IWindow window = dictionary.getWindow(this.dialog);
            if (window == null)
            {
                super.setError("Window " + this.dialog + " not found in the dictionary",ErrorKind.DIALOG_NOT_FOUND);
                return;
            }

			logger.debug("Process dialog : " + window);
			IControl element = window.getControlForName(null, frame);
			if (element == null)
			{
				super.setError(message(id, window, SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
				return;
			}
			service.switchToFrame(element.locator());
		}

		this.setResult(null);
	}


}
