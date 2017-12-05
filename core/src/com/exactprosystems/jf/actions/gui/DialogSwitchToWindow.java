////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
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
	public static final String connectionName = "AppConnection";
	public static final String dialogName     = "Dialog";
	public static final String frameName      = "Frame";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_APP_CONNECTION)
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_DIALOG)
	protected String dialog;

	@ActionFieldAttribute(name = frameName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.DIALOG_SWITCH_TO_WINDOW_FRAME)
	protected String frame;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (dialogName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (dialogName.equals(parameterToFill))
		{
			Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
		}
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();
		String id = this.connection.getId();
		
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
			IWindow window = Helper.getWindow(dictionary, this.dialog);

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
