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
import com.exactprosystems.jf.api.app.IWindow.SectionKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameter;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Table;

import java.util.*;

import static com.exactprosystems.jf.actions.gui.ActionGuiHelper.*;

@ActionAttribute(
		group 					= ActionGroups.GUI, 
		suffix					= "DLGFLL",
		generalDescription 		= "Finds or opens dialog window, fills in fields and pushes buttons.", 
		additionFieldsAllowed 	= true, 
		outputDescription 		= "A map wich contains all text of requested fields.", 
		outputType 				= Map.class
	)
public class DialogFill extends AbstractAction
{
	public final static String	connectionName	= "AppConnection";
	public final static String	dialogName		= "Dialog";
	public final static String	doNotOpenName	= "DoNotOpen";
	public final static String	doNotCloseName	= "DoNotClose";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection.")
	protected AppConnection		connection		= null;

	@ActionFieldAttribute(name = dialogName, mandatory = true, description = "A name of the dialog.")
	protected String			dialog			= null;

	@ActionFieldAttribute(name = doNotOpenName, mandatory = false, description = "Do not open a new dialog.")
	protected Boolean			doNotOpen		= false;

	@ActionFieldAttribute(name = doNotCloseName, mandatory = false, description = "Do not close a dialog.")
	protected Boolean			doNotClose		= false;

	public DialogFill()
	{
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		switch (fieldName)
		{
			case dialogName:
			case doNotOpenName:
			case doNotCloseName:
				return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case dialogName:
				ActionGuiHelper.dialogsNames(context, super.owner.getMatrix(), this.connection, list);
				break;
				
			case doNotCloseName:
			case doNotOpenName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
		}
	}

	@Override
	public void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ActionGuiHelper.extraParameters(list, super.owner.getMatrix(), this.connection, Str.asString(parameters.get(dialogName)), parameters);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
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
		IGuiDictionary dictionary = connection.getDictionary();
		IWindow window = dictionary.getWindow(this.dialog);
		Set<ControlKind> supportedControls = new HashSet<>();
		supportedControls.addAll(Arrays.asList(app.getFactory().supportedControlKinds()));

		logger.debug("Process dialog: " + window);

		logger.debug("Check the addition parameters");
		window.checkParams(parameters.select(TypeMandatory.Extra).keySet());
		service.startNewDialog();
		if (!this.doNotOpen)
		{
			SectionKind onOpen = SectionKind.OnOpen;
			logger.debug("Perform " + onOpen);
			for (IControl control : window.getSection(onOpen).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onOpen, control, "is not allowed"));
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (res.isPermittedOperation())
				{
					super.setError(message(id, window, onOpen, control, res.getText()));
					return;
				}
				if (!res.isOk())
				{
					super.setError(message(id, window, onOpen, control, " returned 'false'. Process is stopped."));
					return;
				}
			}
		}

		Map<String, Object> outValue = new LinkedHashMap<>();

		SectionKind run = SectionKind.Run;
		logger.debug("Perform " + run);
		ISection sectionRun = window.getSection(run);
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			String name = parameter.getName();
			Object obj = parameter.getValue();

			IControl control = sectionRun.getControlByIdAndValue(name, obj);
			if (checkControl(supportedControls, control))
			{
				super.setError(message(id, window, run, control, "is not allowed"));
				return;
			}

			OperationResult res = control.operate(service, window, obj);
			if (res.isOk())
			{
				if (res.isColorMapIsFilled())
				{
					outValue.put(name, res.getColorMap());
				}
				else if (res.isMapFilled())
				{
					outValue.put(name, res.getMap());
				}
				else if (res.isArrayFilled())
				{
					outValue.put(name, new Table(res.getArray(), evaluator));
				}
				else
				{
					String val = res.getText();
					outValue.put(name, val);
				}
			}
			else if (res.isPermittedOperation())
			{
				super.setError(message(id, window, run, control, res.getText()));
				return;
			}
			else
			{
				super.setError(message(id, window, run, control, " returned 'false'. Process is stopped."));
				return;
			}
		}

		if (!this.doNotClose)
		{
			SectionKind onClose = SectionKind.OnClose;
			logger.debug("Perform " + onClose);
			for (IControl control : window.getSection(onClose).getControls())
			{
				if (checkControl(supportedControls, control))
				{
					super.setError(message(id, window, onClose, control, "is not allowed"));
					return;
				}

				OperationResult res = control.operate(service, window, null);
				if (res.isPermittedOperation())
				{
					super.setError(message(id, window, onClose, control, res.getText()));
					return;
				}
				if (!res.isOk())
				{
					super.setError(message(id, window, onClose, control, " returned 'false'. Process is stopped."));
					return;
				}
			}
		}

		super.setResult(outValue);
	}

	private boolean checkControl(Set<ControlKind> supportedControls, IControl control) throws Exception
	{
		return !supportedControls.contains(control.getBindedClass());
	}
}
