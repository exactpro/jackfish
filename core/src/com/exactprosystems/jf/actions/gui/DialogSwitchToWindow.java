////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2016, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					   = ActionGroups.GUI,
		constantGeneralDescription = R.DIALOG_SWITCH_TO_WINDOW_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 	       = R.DIALOG_SWITCH_TO_WINDOW_EXAMPLE
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

			default:
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
			service.switchToFrame(null, null);
		}
		else
		{
			if (this.frame == null)
			{
				throw new Exception("Parameter 'frame' is needed for not-null 'dialog' parameter.");
			}
			IGuiDictionary dictionary = this.connection.getDictionary();
			IWindow window = dictionary.getWindow(this.dialog);
			Helper.throwExceptionIfDialogNull(window, this.dialog);

			logger.debug("Process dialog : " + window);
			IControl element = window.getControlForName(null, frame);
			if (element == null)
			{
				super.setError(message(id, window, SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
				return;
			}
			Locator owner = null;
			if(!Str.IsNullOrEmpty(element.getOwnerID()))
			{
				owner = window.getControlForName(null, element.getOwnerID()).locator();
			}
			service.switchToFrame(owner, element.locator());
		}

		this.setResult(null);
	}


}
