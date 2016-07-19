////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2016, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.gui;

import static com.exactprosystems.jf.actions.gui.ActionGuiHelper.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem;
import com.exactprosystems.jf.documents.matrix.parser.items.ErrorKind;

import java.util.List;

@ActionAttribute(
		group = ActionGroups.GUI, 
		suffix = "DLG", 
		generalDescription = "Switch to desired frame", 
		additionFieldsAllowed = false 
	)
public class DialogSwitchToWindow extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";
	public static final String	dialogName		= "Dialog";
	public static final String frameName = "Frame";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connect")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = false, description = "Name of dialog in the dictionary on self element tool will switch to. "
			+ "If is absent tool will switch to the parent frame.")
	protected String			dialog;

	@ActionFieldAttribute(name = frameName, mandatory = false, description = "Name of locator in the dictionary on which the tool will be switch to.")
	protected String 			frame;

	@Override
	public void initDefaultValues() 
	{
		dialog	= null;
		frame	= null;
	}
	
	@Override
	protected ActionItem.HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case dialogName:
				return ActionItem.HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				ActionGuiHelper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
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

			logger.debug("Process dialog : " + window);
			IControl element = window.getControlForName(null, frame);
			if (element == null)
			{
				super.setError(message(id, window, SectionKind.Self, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
				return;
			}
			service.switchToFrame(element.locator());
		}

		this.setResult(null);
	}


}
