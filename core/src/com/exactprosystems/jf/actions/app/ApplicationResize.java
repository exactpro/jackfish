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
		suffix					= "APPSZ",
		generalDescription 		= "The purpose of the action is to change the window size of the application under test.",
		additionFieldsAllowed 	= false,
		examples = "Example №1.\n" +
				"{{##Action;#AppConnection;#Width;#Height\n" +
				"ApplicationResize;app;1000;1000#}}\n" +
				"\n" +
				"\n" +
				"Example №2.\n" +
				"{{##Action;#Maximize;#AppConnection\n" +
				"ApplicationResize;true;app#}}\n",
		seeAlso					=
				"{{@ApplicationStart@}}, {{@ApplicationConnectTo@}}"
	)
public class ApplicationResize extends AbstractAction
{
	public final static String connectionName 	= "AppConnection";
	public final static String heightName 		= "Height";
	public final static String widthName 		= "Width";
	public final static String minimizeName 	= "Minimize";
	public final static String maximizeName 	= "Maximize";
	
	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}." )
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name = heightName, mandatory = false, description = "The window height is changed to the specified height." )
	protected Integer height;

	@ActionFieldAttribute(name = widthName, mandatory = false, description = "The window width is changed to the specified width." )
	protected Integer width;

	@ActionFieldAttribute(name = minimizeName, mandatory = false, description = "If the parameter value is true, it sets the minimum size of the window." )
	protected Boolean minimize;

	@ActionFieldAttribute(name = maximizeName, mandatory = false, description = "If the parameter value is true, it sets the maximum size of the window." )
	protected Boolean maximize;

	public ApplicationResize()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		height 		= null;
		width		= null;
		minimize	= null;
		maximize	= null;
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
			app.service().resize(
					this.height == null ? 0 : this.height.intValue(), 
					this.width == null ? 0 : this.width.intValue(), 
					this.maximize == null ? false : this.maximize.booleanValue(),
					this.minimize == null ? false : this.minimize.booleanValue());
			super.setResult(null);
		}
	}

}
