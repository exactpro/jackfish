/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Services,
		suffix					   = "SRVLD",
		constantGeneralDescription = R.SERVICE_LOAD_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.SERVICE_LOAD_OUTPUT_DESC,
		outputType 				   = ServiceConnection.class,
		constantExamples 		   = R.SERVICE_LOAD_EXAMPLE
	)
public class ServiceLoad extends AbstractAction 
{
	public static final String idName = "ServiceId";

	@ActionFieldAttribute(name = idName, mandatory = true, constantDescription = R.SERVICE_LOAD_ID)
	protected String id;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		return idName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (idName.equals(parameterToFill))
		{
			ActionServiceHelper.serviceNames(list, context.getEvaluator(), context.getConfiguration());
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IServicesPool servicesPool = context.getConfiguration().getServicesPool();
		ServiceConnection connection = servicesPool.loadService(this.id);

		super.setResult(connection);
	}
}
