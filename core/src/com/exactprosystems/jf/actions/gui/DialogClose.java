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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					   = ActionGroups.GUI,
		suffix 					   = "DLGCLS",
		additionFieldsAllowed 	   = false,
		constantGeneralDescription = R.DIALOG_CLOSE_GENERAL_DESC,
		constantOutputDescription  = R.DIALOG_CLOSE_OUTPUT_DESC,
		outputType 				   = Integer.class,
		constantExamples 		   = R.DIALOG_CLOSE_EXAMPLE
)
public class DialogClose extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_CLOSE_APP_CONNECTION)
	protected AppConnection connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_CLOSE_DIALOG)
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
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			default:

		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), false);
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IGuiDictionary dictionary = this.connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		Helper.throwExceptionIfDialogNull(window, this.dialog);
		String id = connection.getId();

		logger.debug("Process dialog : " + window);

		IApplication app = this.connection.getApplication();
		IRemoteApplication service = app.service();
		IControl element = window.getSelfControl();
		
		if (element == null)
		{
			super.setError(message(id, window, SectionKind.Self, null, null, "Self control is not found."), ErrorKind.ELEMENT_NOT_FOUND);
			return;
		}
		
		List<LocatorAndOperation> operations = new ArrayList<>();
		for (IControl control : window.getSection(IWindow.SectionKind.Close).getControls())
		{
			String expression = control.getExpression();
			if (!Str.IsNullOrEmpty(expression))
			{
				Object value = evaluator.evaluate(expression);
				if (value instanceof Operation)
				{
					Operation operation = (Operation)value;
					Iterator<Part> iter = operation.iterator();

					if (iter.hasNext())
					{
						control.prepare(iter.next(), null);
					}
					operations.add(new LocatorAndOperation(control.locator(), operation));
				}
			}
		}
		
		
		int closed = service.closeAll(element.locator(), operations);
		
		super.setResult(closed);
	}
}
