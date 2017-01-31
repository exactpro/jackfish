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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Services,
		suffix					= "SRVSTRT",
		generalDescription 		= "The following action is needed to run a service that was loaded earlier by {{@ServiceLoad@}}.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "Additional parameters depend on the type of the running service.",
		outputDescription 		= "True, if service running successful.",
		examples 				= "{{`1. Load MatrixService`}}"
				+ "{{`2. Load a service woth an additional parameter Port, that was loaded earlier.`}} "
				+ "{{##Id;#Action;#ServiceId"
				+ "SRVLD1;ServiceLoad;'MatrixService'\n"
				+ "\n"
				+ "#Id;#Action;#Port;#ServiceConnection\n"
				+ "SRVSTRT1;ServiceStart;10565;SRVLD1.Out#}}"
	)
public class ServiceStart extends AbstractAction 
{
	public final static String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A connection with a service is specified, that should be run. An output object of {{@ServiceLoad@}} action is indicated." )
	protected ServiceConnection	connection	= null;

	public ServiceStart()
	{
	}
	
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
		}
		else
		{
			IService service = this.connection.getService();
			boolean res = service.start(context, parameters.select(TypeMandatory.Extra));
			if (res)
			{
				super.setResult(null);
			}
			else
			{
				super.setError("Connection can not be established. Possibly the port is in use.", ErrorKind.SERVICE_ERROR);
			}
		}
	}

	@Override
	public void initDefaultValues() 
	{
		
	}

}
