////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.services;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.service.IServicesPool;
import com.exactprosystems.jf.api.service.ServiceConnection;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Services,
		suffix					   = "SRVSTP",
		constantGeneralDescription = R.SERVICE_STOP_GENERAL_DESC,
		additionFieldsAllowed      = false,
		constantExamples 		   = R.SERVICE_STOP_EXAMPLE
	)
public class ServiceStop extends AbstractAction
{
	public final static String connectionName = "ServiceConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.SERVICE_STOP_CONNECTION )
	protected ServiceConnection	connection	= null;

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
			servicesPool.stopService(connection);
			super.setResult(null);
		}
		catch (Exception e)
		{
			super.setError(e.getMessage(), ErrorKind.SERVICE_ERROR);
		}
	}
}
