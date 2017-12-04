////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.App,
		suffix					   = "APPSTP",
		constantGeneralDescription = R.APP_STOP_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples           = R.APP_STOP_EXAMPLE,
		seeAlsoClass               = {ApplicationConnectTo.class, ApplicationStart.class}
	)
public class ApplicationStop extends AbstractAction 
{
	public static final String connectionName = "AppConnection";
	public static final String needKillName = "Kill";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_STOP_CONNECTION)
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name = needKillName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.APPLICATION_STOP_NEED_KILL)
	protected Boolean needKill;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		context.getConfiguration().getApplicationPool().stopApplication(this.connection, this.needKill);
		super.setResult(null);
	}
}
