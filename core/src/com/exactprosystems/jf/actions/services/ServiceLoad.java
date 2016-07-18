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
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

import java.util.List;

@ActionAttribute(
		group					= ActionGroups.Services,
		suffix					= "SRVLD",
		generalDescription 		= "Loads desired service and inits it. ",
		additionFieldsAllowed 	= false,
		outputDescription = "Connection to the service.", 
		outputType = ServiceConnection.class
	)
public class ServiceLoad extends AbstractAction 
{
	public final static String idName = "ServiceId";

	@ActionFieldAttribute(name = idName, mandatory = true, description = "The service id." )
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
	public void doRealDocumetation(Context context, ReportBuilder report)
	{
		ReportTable info = report.addTable("Available services", true, 100, new int[] { 100 }, "Service name");
		for (String protocol : context.getConfiguration().getServicesPool().servicesNames())
		{
			info.addValues(protocol);
		}
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IServicesPool service = context.getConfiguration().getServicesPool();
		ServiceConnection connection = service.loadService(this.id);
		
		super.setResult(connection);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}
