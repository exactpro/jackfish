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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.App,
		suffix					= "APPSTP",
		generalDescription 		= "The purpose of the action is to close the application under test. As a rule,"
				+ " ApplicationStop is placed in one of the last TestCases to stop the application initialized in"
				+ " {{@ApplicationStart@}} or {{@ApplicationConnectTo@}}. The flag {{$“Global”$}} (indicated with letter “G”)"
				+ " should be set in actions {{@ApplicationStart@}} or {{@ApplicationConnectTo@}}.",
		additionFieldsAllowed 	= false,
		examples = "{{##Action;#AppConnection\n" +
				"ApplicationStop;app\n#}}",
		seeAlsoClass = {ApplicationConnectTo.class, ApplicationStart.class}
	)
public class ApplicationStop extends AbstractAction 
{
	public static final String connectionName = "AppConnection";
	public static final String needKillName = "Kill";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies the"
			+ " started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to. It is the output value of such actions"
			+ " as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name=needKillName, mandatory = false, def = DefaultValuePool.False, description = "If true, the process will killed")
	protected Boolean needKill;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		boolean kill = needKill.booleanValue();
		IApplication app = this.connection.getApplication();
		app.stop(kill);
		super.setResult(null);
	}
}
