////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group 					   = ActionGroups.App,
		constantGeneralDescription = R.APP_MOVE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		examples 				   = "", // TODO add examples here. VMF 03.05.17
		seeAlsoClass 			   = {ApplicationStart.class, ApplicationConnectTo.class}
)
public class ApplicationMove extends AbstractAction
{
	public static final String connectionName = "AppConnection";
	public static final String xName = "X";
	public static final String yName = "Y";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_MOVE_CONNECTION )
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = xName, mandatory = true, constantDescription = R.APPLICATION_MOVE_X)
	protected Integer x;

	@ActionFieldAttribute(name = yName, mandatory = true, constantDescription = R.APPLICATION_MOVE_Y)
	protected Integer y;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication application = this.connection.getApplication();
		application.service().moveWindow(this.x, this.y);
		super.setResult(null);
	}
}
