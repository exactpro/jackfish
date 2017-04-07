////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Services,
		suffix					= "SRVLD",
		generalDescription 		= "The following action is needed to load a service, to initialize it and to connect to it."
				+ "Services are things functioning as a server in a client-server relationship."
				+ "A server listens to a definite port and allows installing TCP connection from the client side.",
		additionFieldsAllowed 	= false,
		outputDescription = "Is a connection with a loaded server. It is used for further interaction with the loaded server.",
		outputType 				= ServiceConnection.class,
		examples 				= "{{##Id;#Action;#ServiceId\n"
				+ "SRVLD1;ServiceLoad;'TEST'#}}"
	)
public class ServiceLoad extends AbstractAction 
{
	public final static String idName = "ServiceId";

	@ActionFieldAttribute(name = idName, mandatory = true, description = "A service ID that was described in Service "
			+ "entries configuration, which will be used for booting a relevant service.If no entry is found in the "
			+ "configuration at such ServiceId, an action finishes with an error." )
	protected String 		id	= null;

	public ServiceLoad()
	{
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return idName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Configuration configuration = context.getConfiguration();
		switch (parameterToFill)
		{
			case idName:
				ActionServiceHelper.serviceNames(list, context.getEvaluator(), configuration);
				break;

			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try
		{
			IServicesPool servicesPool = context.getConfiguration().getServicesPool();
			ServiceConnection connection = servicesPool.loadService(this.id);
			
			super.setResult(connection);
		}
		catch (Exception e)
		{
			super.setError(e.getMessage(), ErrorKind.SERVICE_ERROR);
		}
	}

	@Override
	public void initDefaultValues()
	{

	}

}
