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
		suffix					= "APPSW",
		generalDescription 		= "Switch main focus to decired window",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Name of window that got focus.",
		outputType				= String.class
	)
public class ApplicationSwitchTo extends AbstractAction
{
	public final static String connectionName = "AppConnection";
	public final static String titleName = "Title";
	public final static String softConditionName = "SoftCondition";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection." )
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "A title of window.")
	protected String 				title	= null;

	@ActionFieldAttribute(name = softConditionName, mandatory = false, description = "Compare window titles and title parameter via contains().")
	protected Boolean 				softCondition;

	public ApplicationSwitchTo()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		softCondition	= true;
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
			String res = app.service().switchTo(this.title, this.softCondition);
			
			if (res.equals(""))
			{
				super.setError("Can not find the window.", ErrorKind.ELEMENT_NOT_FOUND);
			}
			else
			{
				super.setResult(res);
			}
		}
	}



}
