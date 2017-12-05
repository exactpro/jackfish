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
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import static com.exactprosystems.jf.actions.gui.Helper.message;

@ActionAttribute(
		group 				  		  = ActionGroups.GUI,
		constantGeneralDescription 	  = R.DIALOG_VALIDATE_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.DIALOG_VALIDATE_ADDITIONAL_DESC,
		constantExamples 			  = R.DIALOG_VALIDATE_EXAMPLE
	)
public class DialogValidate extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";
	public static final String	dialogName		= "Dialog";
	public static final String	doNotOpenName	= "DoNotOpen";
	public static final String	doNotCloseName	= "DoNotClose";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.DIALOG_VALIDATE_APP_CONNECTION)
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, constantDescription = R.DIALOG_VALIDATE_DIALOG)
	protected String			dialog			= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_VALIDATE_DO_NOT_OPEN)
	protected Boolean			doNotOpen;

	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.DIALOG_VALIDATE_DO_NOT_CLOSE)
	protected Boolean			doNotClose;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case dialogName:
			case doNotOpenName:
			case doNotCloseName:
				return HelpKind.ChooseFromList;
			case connectionName:
				return null;
			default:
				return HelpKind.ChooseFromList;
		}
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				Helper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;

			case doNotCloseName:
			case doNotOpenName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
				
			default:
				list.add(new ReadableValue("-1", "Do not validate"));
				list.add(new ReadableValue("0"));
				list.add(new ReadableValue("1"));
				list.add(new ReadableValue("2"));
				list.add(new ReadableValue("3"));
		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), false);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = Helper.getApplication(this.connection);
		IRemoteApplication service = app.service();
		String id = this.connection.getId();
		IGuiDictionary dictionary = app.getFactory().getDictionary();
		IWindow window = Helper.getWindow(dictionary, this.dialog);
		
		Set<ControlKind> supportedControls = app.getFactory().supportedControlKinds();

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		window.checkParams(parameters.select(TypeMandatory.Extra).keySet());

		if (!this.doNotOpen)
		{
			SectionKind onOpen = SectionKind.OnOpen;
			logger.debug("Perform " + onOpen);
			for (IControl control : window.getSection(onOpen).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onOpen, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, res.getLocator(), "" + res.getValue()), ErrorKind.NOT_EQUAL);
					return;
				}
			}
		}

		boolean totalResult = true;
		ReportTable table = report.addTable("Dialog validation", null, true, true, new int[] { 25, 65 }, "Field", "Result");
		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			IControl control = sectionRun.getControlById(parameter.getName());
			if (control == null)
			{
				super.setError(message(id, window, run, null, null, "control with name '" + parameter.getName() + "' not found in the dictionry"), ErrorKind.LOCATOR_NOT_FOUND);
				return;
			}

			int expectedSize = ((Number) parameter.getValue()).intValue();
			if (expectedSize >= 0)
			{
				IControl owner = window.getOwnerControl(control);
				Locator ownerLocator = owner == null ? null : owner.locator();
				Locator controlLocator = control.locator();

				Collection<String> found = service.findAll(ownerLocator, controlLocator);
				int actualSize = found.size();

				if (expectedSize != actualSize)
				{
					totalResult = false;
				}

				table.addValues(parameter.getName(), expectedSize == actualSize ? "Passed" : ("Failed expected: " + expectedSize + " actual: " + actualSize));
			}
		}

		if (!totalResult)
		{
			super.setError(message(id, window, run, null, null, "Dialog verifing failed."), ErrorKind.FAIL);
			return;
		}

		if (!this.doNotClose)
		{
			SectionKind onClose = SectionKind.OnClose;
			logger.debug("Perform " + onClose);
			for (IControl control : window.getSection(onClose).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onClose, control, null, "is not allowed"), ErrorKind.OPERATION_NOT_ALLOWED);
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, res.getLocator(), " returned 'false'. Process is stopped."), ErrorKind.NOT_EQUAL);
					return;
				}
			}
		}

		super.setResult(null);
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control)
	{
		return !supportedControls.contains(control.getBindedClass());
	}

}
