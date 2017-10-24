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
import com.exactprosystems.jf.actions.gui.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group						  = ActionGroups.App,
		suffix						  = "APPSTR",
		additionFieldsAllowed 		  = true,
		outputType					  = AppConnection.class,
		constantGeneralDescription    = R.APP_START_GENERAL_DESC,
		constantAdditionalDescription = R.APP_START_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.APP_START_OUTPUT_DESC,
		constantExamples 			  = R.APP_START_EXAMPLE,
		seeAlsoClass 				  = {ApplicationStop.class, ApplicationConnectTo.class, ApplicationGetProperties.class,
				ApplicationNewInstance.class, ApplicationRefresh.class, ApplicationResize.class, ApplicationSwitchTo.class,
				DialogAlert.class, DialogCheckLayout.class,	DialogClose.class, DialogFill.class, DialogSwitchToWindow.class}
	)
public class ApplicationStart extends AbstractAction 
{
	public static final String idName 	= "AppId";


	@ActionFieldAttribute(name = idName, mandatory = true, constantDescription = R.APPLICATION_START_APP_ID)
	protected String id	= null;
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.START, this.owner.getMatrix(), context, parameters, idName, null);
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
				res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, idName, null, fieldName);
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
				Helper.applicationsNames(list, context);
				break;

			default:
				Helper.fillListForParameter(list, this.owner.getMatrix(), context, parameters, idName, null, parameterToFill);
				break;
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
		
		AppConnection connection = pool.startApplication(this.id, args);
		if (connection.isGood())
		{
			super.setResult(connection);
		}
		else
		{
			super.setError("Application is not started.", ErrorKind.APPLICATION_ERROR);
		}
	}
}
