////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.service.IService;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Services,
		suffix					= "SRVSTRT",
		generalDescription 		= "Starts desired service. ",
		additionFieldsAllowed 	= true,
		outputDescription 		= "True, if service starts successful."
	)
public class ServiceStart extends AbstractAction 
{
	public final static String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The service connection." )
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
		for (String str : connection.getService().getFactory().wellKnownStartArgs())
		{
			list.add(new ReadableValue(str));
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null");
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
				super.setError("Connection can not be established.");
			}
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}
