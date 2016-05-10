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
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPSZ",
		generalDescription 		= "Resize the main window of application",
		additionFieldsAllowed 	= false
	)
public class ApplicationResize extends AbstractAction
{
	public final static String connectionName 	= "AppConnection";
	public final static String heightName 		= "Height";
	public final static String widthName 		= "Width";
	public final static String minimizeName 	= "Minimize";
	public final static String maximizeName 	= "Maximize";
	
	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "The application connection." )
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name = heightName, mandatory = false, description = "The hight that will be set for the main window of application." )
	protected Integer height;

	@ActionFieldAttribute(name = widthName, mandatory = false, description = "The width that will be set for the main window of application." )
	protected Integer width;

	@ActionFieldAttribute(name = minimizeName, mandatory = false, description = "The main window of application will be minimized." )
	protected Boolean minimize;

	@ActionFieldAttribute(name = maximizeName, mandatory = false, description = "The main window of application will be maximized." )
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
			super.setError("Connection is null");
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
