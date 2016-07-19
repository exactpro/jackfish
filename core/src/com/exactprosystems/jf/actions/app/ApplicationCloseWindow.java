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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ErrorKind;

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPCW",
		generalDescription		= "Close current window on application",
		additionFieldsAllowed	= false,
		outputDescription		= "Name of closed window",
		outputType				= String.class
)
public class ApplicationCloseWindow extends AbstractAction
{
	public final static String connectionName = "AppConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection")
	protected AppConnection connection = null;

	public ApplicationCloseWindow()
	{
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
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

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
