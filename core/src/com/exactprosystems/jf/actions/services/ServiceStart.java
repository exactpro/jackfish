////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group						  = ActionGroups.Services,
		suffix						  = "SRVSTRT",
		constantGeneralDescription    = R.SERVICE_START_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.SERVICE_START_ADDITIONAL_DESC,
		constantOutputDescription     = R.SERVICE_START_OUTPUT_DESC,
		constantExamples 			  = R.SERVICE_START_EXAMPLE
	)
public class ServiceStart extends AbstractAction 
{
	public final static String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A connection with a service is specified, that should be run. An output object of {{@ServiceLoad@}} action is indicated." )
	protected ServiceConnection	connection	= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		ServiceConnection connection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		if (connection != null)
		{
			return connection.getService().getFactory().canFillParameter(fieldName) ? HelpKind.ChooseFromList : null;
		}
		
		return null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		ServiceConnection connection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		if (connection != null)
		{
			String[] arr = connection.getService().getFactory().listForParameter(parameterToFill);
			for (String str : arr)
			{
				list.add(new ReadableValue(context.getEvaluator().createString(str)));
			}
		}
	}
	

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ServiceConnection connection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		for (String str : connection.getService().getFactory().wellKnownParameters(ParametersKind.START))
		{
			list.add(new ReadableValue(str));
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		try
		{
			IServicesPool servicesPool = context.getConfiguration().getServicesPool();
			servicesPool.startService(context, this.connection, parameters.select(TypeMandatory.Extra).makeCopy());
			super.setResult(null);
		}
		catch (Exception e)
		{
			super.setError(e.getMessage(), ErrorKind.SERVICE_ERROR);
		}
	}
}
