////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group = ActionGroups.App,
		suffix = "APPNI",
		generalDescription = "Open new window",
		additionFieldsAllowed = true
)
public class ApplicationNewInstance extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String tabName = "Tab";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection." )
	protected AppConnection connection	= null;

	@ActionFieldAttribute(name = tabName, mandatory = false, description = "Opened new tab")
	protected Boolean tab;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (tabName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		
		parameters.evaluateAll(context.getEvaluator());
		connection = ApplicationHelper.checkConnection(connection, parameters.get(connectionName));
		if (connection != null)
		{
			connection.getApplication().getFactory().canFillParameter(fieldName);
		}	
		
		return null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case tabName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
				
			default:
				parameters.evaluateAll(context.getEvaluator());
				connection = ApplicationHelper.checkConnection(connection, parameters.get(connectionName));
				if (connection != null)
				{
					String[] arr = connection.getApplication().getFactory().listForParameter(parameterToFill);
					for (String str : arr)
					{
						list.add(new ReadableValue(context.getEvaluator().createString(str)));
					}
				}	
				break;
		}
	}
	
	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			Map<String, String> args = new HashMap<>();
			args.put(tabName, String.valueOf(this.tab));
			for (Parameter parameter : parameters.select(TypeMandatory.Extra))
			{
				args.put(parameter.getName(), String.valueOf(parameter.getValue()));
			}
			IApplication app = connection.getApplication();
			app.service().newInstance(args);
			super.setResult(null);
		}
	}

	@Override
	public void initDefaultValues() 
	{
		tab = false;
	}
}
