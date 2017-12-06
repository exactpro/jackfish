////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.Arrays;
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
	public static final String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SERVICE_START_CONNECTION)
	protected ServiceConnection connection;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		ServiceConnection serviceConnection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		return serviceConnection.getService().getFactory().canFillParameter(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		ServiceConnection serviceConnection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		Arrays.stream(serviceConnection.getService().getFactory().listForParameter(parameterToFill))
				.map(context.getEvaluator()::createString)
				.map(ReadableValue::new)
				.forEach(list::add);
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		ServiceConnection serviceConnection = ActionServiceHelper.checkConnection(parameters.get(connectionName));
		Arrays.stream(serviceConnection.getService().getFactory().wellKnownParameters(ParametersKind.START))
				.map(ReadableValue::new)
				.forEach(list::add);
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IServicesPool servicesPool = context.getConfiguration().getServicesPool();
		servicesPool.startService(context, this.connection, parameters.select(TypeMandatory.Extra).makeCopy());
		super.setResult(null);
	}
}
