////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@ActionAttribute(
		group = ActionGroups.GUI,
		suffix = "DLGCLS",
		additionFieldsAllowed = false,
		generalDescription = "Close dialogs on windows, that described on section close",
		outputDescription 		= "How many dialogs of this kind were closed.", 
		outputType 				= Integer.class
)
public class DialogClose extends AbstractAction
{
	private static final Logger logger = Logger.getLogger(DialogClose.class);

	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "A name of the dialog.")
	protected String			dialog			= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return dialogName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				ActionGuiHelper.dialogsNames(context, this.connection, list);
				break;

			default:

		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ActionGuiHelper.extraParameters(list, context, this.connection, Str.asString(parameters.get(dialogName)), parameters);
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IGuiDictionary dictionary = this.connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);

		logger.debug("Process dialog : " + window);

		IApplication app = this.connection.getApplication();
		IRemoteApplication service = app.service();
		IControl element = window.getSelfControl();
		
		List<LocatorAndOperation> operations = new ArrayList<LocatorAndOperation>();
		for (IControl control : window.getSection(IWindow.SectionKind.Close).getControls())
		{
			Operation operation = Operation.create();
			Iterator<Part> iter = operation.iterator();

			if (iter.hasNext())
			{
				control.prepare(iter.next(), null);
			}
			operations.add(new LocatorAndOperation(control.locator(), operation));
		}
		
		int closed = service.closeAll(element.locator(), operations);
		
		super.setResult(closed);
	}
}
