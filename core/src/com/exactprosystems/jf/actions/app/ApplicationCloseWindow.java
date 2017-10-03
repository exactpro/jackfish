////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
		group					= ActionGroups.App,
		suffix					= "APPCW",
		generalDescription		=
				"Plug-in dependent action. The purpose of the action is to close the currently active window of the"
				+ " application. When the application is a browser, the action closes the currently active tab.",
		additionFieldsAllowed	= false,
		outputDescription		= "Returns the title bar of the window which was closed.",
		outputType				= String.class,
		examples = "{{#\n" +
				"#Id;#Action;#AppConnection\n"
				+ "ACW;ApplicationCloseWindow;app\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "!Str.IsNullOrEmpty(ACW.Out);'String is null or empty'#}}",
        seeAlsoClass            = { ApplicationStart.class, ApplicationConnectTo.class }
)
public class ApplicationCloseWindow extends AbstractAction
{
	@ActionFieldAttribute(constantName = R.Constants.APP_CONNECTION_NAME, mandatory = true, constantDescription = R.Constants.APP_CONNECTION_NAME_DESCRIPTION)
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
