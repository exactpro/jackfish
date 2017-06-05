////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.app.PerformKind;
import com.exactprosystems.jf.api.common.SerializablePair;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.rmi.RemoteException;
import java.util.List;

@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "DLGALRT",
		generalDescription		= "The purpose of this action is to process pop-up notifications.\n"
				+ "It is a plug-in dependent action currently being used only with web plug-in.",
		additionFieldsAllowed	= false,
		outputDescription		= "Output value is the notification heading.",
		outputType				= String.class,
		examples 				= "{{`1. Type 'hello' in the pop-up notification field`}}"
				+ "{{`2. Ensure we have the notification heading as an output value.`}}"
				+ "{{##Id;#Action;#Text;#Perform;#AppConnection\n"
				+ "DLGALRT1;DialogAlert;'hello';PerformKind.Accept;APPSTR1.Out\n"
				+ "#Assert;#Message\n"
				+ "DLGALRT1.Out != null;#}}"
	)
public class DialogAlert extends AbstractAction
{
	public static final String connectionName	= "AppConnection";
	public static final String performName		= "Perform";
	public static final String textName			= "Text";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = performName, mandatory = true, description = "The PerformKind type. The parameter "
			+ "responsible for actions with alert.  There are 3 states to the parameter:\n"
			+ "Nothing – ignore the notification.\n"
			+ "Accept – click “OK”.\n"
			+ "Dismiss – click “Cancel”.")
	protected PerformKind perform = null;

	@ActionFieldAttribute(name = textName, mandatory = false, def = DefaultValuePool.Null, description = "Used to input text in the corresponding field of notification.")
	protected String text;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case performName:
				return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		list.add(new ReadableValue("PerformKind.Accept", "Press ok"));
		list.add(new ReadableValue("PerformKind.Dismiss", "Press cancel"));
		list.add(new ReadableValue("PerformKind.Nothing", "Do nothing"));
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError(String.format("Field with name '%s' can't be null", connectionName), ErrorKind.EMPTY_PARAMETER);
			return;
		}
		IApplication app = connection.getApplication();
		String id = connection.getId();
		IRemoteApplication service = app.service();
		if (service == null)
		{
			super.setError(String.format("App with id '%s' not started yet", id), ErrorKind.APPLICATION_ERROR);
			return;
		}
		if (this.perform == null)
		{
			super.setError(String.format("Field with name '%s' can't be null", performName), ErrorKind.EMPTY_PARAMETER);
			return;
		}

		SerializablePair<String, Boolean> alertText;
		try
		{
			alertText = service.getAlertText();
		}
		catch (RemoteException e)
		{
			logger.error(e.getMessage(), e);
			super.setError(e.getMessage(), ErrorKind.EXCEPTION);
			return;
		}
		service.setAlertText(this.text, this.perform);
		this.setResult(alertText.getKey());
	}
}
