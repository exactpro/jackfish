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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group						= ActionGroups.App,
		suffix						= "APPCW",
		constantGeneralDescription	= R.APP_CLOSE_WINDOW_GENERAL_DESCRIPTION,
		additionFieldsAllowed		= false,
		constantOutputDescription	= R.APP_CLOSE_WINDOW_OUTPUT_DESCRIPTION,
		outputType					= String.class,
		constantExamples			= R.APP_CLOSE_WINDOW_EXAMPLES,
        seeAlsoClass				= { ApplicationStart.class, ApplicationConnectTo.class }
)
public class ApplicationCloseWindow extends AbstractAction
{
	public static final String APP_CONNECTION_NAME = "AppConnection";

	@ActionFieldAttribute(name = APP_CONNECTION_NAME, mandatory = true, constantDescription = R.APP_CONNECTION_NAME_DESCRIPTION)
	protected AppConnection connection = null;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = connection.getApplication();
		String res = app.service().closeWindow();

		if (res.equals(""))
		{
			super.setError("Can not close the window", ErrorKind.ELEMENT_NOT_FOUND);
		}
		else
		{
			super.setResult(res);
		}
	}
}
