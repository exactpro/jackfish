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
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPSTR",
		generalDescription 		= "Connects to the application under tests.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "Connection to the application.",
		outputType				= AppConnection.class
	)
public class ApplicationConnectTo extends AbstractAction 
{
	public static final String idName 	= "AppId";


	@ActionFieldAttribute(name = idName, mandatory = true, description = "The application id." )
	protected String 		id	= null;
	
	public ApplicationConnectTo()
	{
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		parameters.evaluateAll(context.getEvaluator());
		for (String str : context.getConfiguration().getApplicationPool().wellKnownConnectArgs(Str.asString(parameters.get(idName))))
		{
			list.add(new ReadableValue(str));
		}
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		boolean res = false;
		switch (fieldName)
		{
			case idName:
				res = true;
				break;
				
			default:
				res = ApplicationHelper.canFillParameter(parameters, context, idName, fieldName);
				break;
		}	
		
		return res ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case idName:
				ApplicationHelper.applicationsNames(list, context);
				break;

			default:
				ApplicationHelper.fillListForParameter(list, context, parameters, idName, parameterToFill);
				break;
		}
	}

	@Override
	public void doRealDocumetation(Context context, ReportBuilder report)
	{
		ReportTable info = report.addTable("Available apps", true, 100, new int[] { 100 }, "App name");
		for (String app : context.getConfiguration().getApplicationPool().appNames())
		{
			info.addValues(app);
		}
	}


	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplicationPool pool = context.getConfiguration().getApplicationPool();

		Map<String, String> args = new HashMap<>();
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			args.put(parameter.getName(), Str.asString(parameter.getValue()));
		}
		
		AppConnection connection = pool.connectToApplication(this.id, args);
		if (connection.isGood())
		{
			super.setResult(connection);
		}
		else
		{
			super.setError("Application is not found.");
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
