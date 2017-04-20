////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 					= ActionGroups.GUI,
		suffix 					= "DLGCLS",
		additionFieldsAllowed 	= false,
		generalDescription 		= "The purpose of the action is to close the dialogs.  The dialog to be closed needs to"
				+ " be described in the Close section of the dictionary. When working with swing and win plug-ins, Dialogs"
				+ " can be closed without being described in the Close section because there is a capability to programmatically"
				+ " close the window. In web the elements can’t be closed programmatically and require direct handling.",
		outputDescription 		= "The number of dialogs closed. ",
		outputType 				= Integer.class,
		examples 				= "{{`1. Start the web application.`}}"
				+ "{{`2. Close all the elements described in the Close section of MyDialog.`}}"
				+ "{{`3. Check the number of closed elements.`}}"
				+ "{{##Id;#Action;#Browser;#URL;#AppId\n"
				+ "APPSTR1;ApplicationStart;'Chrome';'https://google.com';'WEB'\n"
				+ "#Id;#Action;#Dialog;#AppConnection\n"
				+ "DLGCLS1;DialogClose;'MyDialog';APPSTR1.Out\n"
				+ "#Assert;#Message\n"
				+ "DLGCLS1.Out > 0;'0 elements was closed'#}}"
)
public class DialogClose extends AbstractAction
{
	private static final Logger logger = Logger.getLogger(DialogClose.class);

	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "The name of the Dialog whose Close section has the descriptions of elements to be closed.")
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
		
		List<LocatorAndOperation> operations = new ArrayList<LocatorAndOperation>();
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

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
