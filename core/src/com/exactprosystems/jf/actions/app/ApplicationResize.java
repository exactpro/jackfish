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

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPSZ",
		generalDescription 		= "The purpose of the action is to change the window size of the application under test.",
		additionFieldsAllowed 	= false,
		examples = "Example №1."
				+ "{{##Action;#AppConnection;#Width;#Height\n"
				+ "ApplicationResize;app;1000;1000#}}"
				+ "Example №2."
				+ "{{##Action;#Maximize;#AppConnection\n"
				+ "ApplicationResize;true;app#}}",
		seeAlsoClass = {ApplicationStart.class, ApplicationConnectTo.class}
	)
public class ApplicationResize extends AbstractAction
{
	public final static String connectionName 	= "AppConnection";
	public final static String heightName 		= "Height";
	public final static String widthName 		= "Width";
	public final static String minimizeName 	= "Minimize";
	public final static String maximizeName 	= "Maximize";
	public final static String normalName		= "Normal";

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

	@ActionFieldAttribute(name = normalName, mandatory = false, description = "If the parameter value is true, it sets normal size of the window.")
	protected Boolean normal;

	@Override
	public void initDefaultValues() 
	{
		this.height 	= null;
		this.width		= null;
		this.minimize	= null;
		this.maximize	= null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplication app = connection.getApplication();
		app.service().resize(
				this.height 	== null ? 0 : this.height.intValue(), 
				this.width 		== null ? 0 : this.width.intValue(), 
				this.maximize 	== null ? false : this.maximize.booleanValue(),
				this.minimize 	== null ? false : this.minimize.booleanValue(),
				this.normal 	== null ? false : this.normal.booleanValue()
		);
		super.setResult(null);
	}
}
