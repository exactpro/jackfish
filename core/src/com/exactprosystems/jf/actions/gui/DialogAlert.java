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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.rmi.RemoteException;
import java.util.List;

@ActionAttribute(
		group					= ActionGroups.GUI,
		suffix					= "DLGALRT",
		generalDescription		= "Switch to alert and set text ( if not null) and switch perform",
		additionFieldsAllowed	= false,
		outputDescription		= "Return text of alert",
		outputType				= String.class
	)
public class DialogAlert extends AbstractAction
{
	public static final String connectionName	= "AppConnection";
	public static final String performName		= "Perform";
	public static final String textName			= "Text";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connect")
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = performName, mandatory = true, description = "If perform equals Accept - just press Ok. If equals Dismiss - press Cancel. If equals Nothing - do nothing")
	protected PerformKind perform = null;

	@ActionFieldAttribute(name = textName, mandatory = false, description = "Text, which will be write in alert")
	protected String text = null;

	@Override
	protected ActionItem.HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case performName:
				return ActionItem.HelpKind.ChooseFromList;
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
		if (this.perform == null)
		{
			throw new Exception(String.format("Field with name '%s' can't be null", performName));
		}

		SerializablePair<String, Boolean> alertText;
		try
		{
			 alertText = service.getAlertText();
		}
		catch (RemoteException e)
		{
			super.setError("Alert is not presented");
			return;
		}
		service.setAlertText(this.text, this.perform);
		this.setResult(alertText.getKey());
	}
}
