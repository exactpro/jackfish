////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
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
		examples = "Example 1."
				+ "{{#\n" +
				"#Action;#AppConnection;#Width;#Height\n"
				+ "ApplicationResize;app;1000;1000\n" +
				"#}} \n"
				+ "Example 2."
				+ "{{#\n" +
				"#Action;#Maximize;#AppConnection\n"
				+ "ApplicationResize;true;app\n" +
				"#}}",
		seeAlsoClass = {ApplicationStart.class, ApplicationConnectTo.class}
	)
//TODO replace max/min/normal to enum
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

	@ActionFieldAttribute(name = heightName, mandatory = false, def = DefaultValuePool.Null, description = "The window height is changed to the specified height." )
	protected Integer height;

	@ActionFieldAttribute(name = widthName, mandatory = false, def = DefaultValuePool.Null, description = "The window width is changed to the specified width." )
	protected Integer width;

	@ActionFieldAttribute(name = minimizeName, mandatory = false, def = DefaultValuePool.Null, description = "If the parameter value is true, it sets the minimum size of the window." )
	protected Boolean minimize;

	@ActionFieldAttribute(name = maximizeName, mandatory = false, def = DefaultValuePool.Null, description = "If the parameter value is true, it sets the maximum size of the window." )
	protected Boolean maximize;

	@ActionFieldAttribute(name = normalName, mandatory = false, def = DefaultValuePool.Null, description = "If the parameter value is true, it sets normal size of the window.")
	protected Boolean normal;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.minimize == null && this.maximize == null && this.normal == null && this.width == null && this.height == null)
		{
			setError("No one resizing parameter is filled.", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		if (checkBoolean(maximizeName, this.maximize, parameters) || checkBoolean(minimizeName, this.minimize, parameters) || checkBoolean(normalName, this.normal, parameters))
		{
			return;
		}

		if (checkInt(widthName, this.width, parameters) || checkInt(heightName, this.height, parameters))
		{
			return;
		}

		boolean max = this.maximize == null ? false : this.maximize;
		boolean min = this.minimize == null ? false : this.minimize;
		boolean nor = this.normal == null ? false : this.normal;

		if ((max == min) || (max == nor) || (min == nor))
		{
			setError(String.format("Need set on the parameters [%s,%s,%s]", maximizeName, minimizeName, normalName), ErrorKind.WRONG_PARAMETERS);
			return;
		}

		if ((this.maximize != null || this.minimize != null || this.normal != null) && (this.height != null || this.width != null))
		{
			setError("Need set state or width, but no both together", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		if ((this.height == null && this.width != null) || (this.height != null && this.width == null))
		{
			setError("Need set both the parameters " + widthName + " and " + heightName, ErrorKind.WRONG_PARAMETERS);
			return;
		}

		IApplication app = connection.getApplication();
		app.service().resize(
				this.height 	== null ? 0 : this.height,
				this.width 		== null ? 0 : this.width,
				max, min, nor
		);
		super.setResult(null);
	}

	private boolean checkBoolean(String keyName, Object value, Parameters parameters)
	{
		return check(keyName, value, parameters, "Parameter " + keyName + " must be true or false");
	}

	private boolean checkInt(String keyName, Object value, Parameters parameters)
	{
		return check(keyName, value, parameters, "Parameter " + keyName + " must be from 0 to " + Integer.MAX_VALUE);
	}

	private boolean check(String keyName, Object value, Parameters parameters, String message)
	{
		if (parameters.getByName(keyName) != null && value == null)
		{
			setError(message, ErrorKind.WRONG_PARAMETERS);
			return true;
		}
		return false;
	}
}
