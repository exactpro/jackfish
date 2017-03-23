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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPR",
		generalDescription 		= "Plug-in dependent action. The purpose of the action is to refresh the application "
				+ "window. It should be used only with web plug-in.",
		additionFieldsAllowed 	= false,
		examples = "{{##Action;#AppConnection\n" +
				"ApplicationRefresh;app\n#}}",
		seeAlsoClass = {ApplicationStart.class, ApplicationConnectTo.class}
	)
public class ApplicationRefresh extends AbstractAction
{
	public static final String connectionName = "AppConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection	connection	= null;


	public ApplicationRefresh()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.connection == null)
		{
			super.setError("Connection is null", ErrorKind.EMPTY_PARAMETER);
		}
		else
		{
			IApplication app = connection.getApplication();
			app.service().refresh();
			super.setResult(null);
		}
	}

	@Override
	public void initDefaultValues() 
	{
		// TODO Auto-generated method stub
	}
}
