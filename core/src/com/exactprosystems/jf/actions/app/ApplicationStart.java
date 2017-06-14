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
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.actions.gui.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
group					= ActionGroups.App,
suffix					= "APPSTR",
additionFieldsAllowed 	= true,
outputType				= AppConnection.class,
generalDescription 		= 
	  "The purpose of the action is to launch the application under test. "
	+ "The type of the application is determined by the chosen plug-in (see also {{@GUI plug-ins@}}). "
	+ "The action requires some additional parameters, which depend on the type of the plug-in used. "
	+ "The parameters are not mandatory from the standpoint of the tool itself, but they are required "
	+ "for the plug-in to launch the application correctly.",

additionalDescription	= 
	  "The structure and meaning of parameters depend on the plug-in used. For example, web.jar "
	+ "requires the following list:" 
	+ "{{` {{$Browser$}} - the browser in which the web application is started, `}}"
	+ "{{` {{$URL$}} - the Internet link to the application server. `}}"
	+ "In order to avoid errors in writing these additional parameters, the user can add them by using "
	+ "the matrices editor accessible via the 'All parameters...' context menu after the {{$AppId$}} parameter "
	+ "has been filled (and is filled by the constant string).",

outputDescription 		= 
	  "A special object which identifies the started application session. "
	+ "This object is required in many other actions to specify the session of the application "
	+ "the indicated action belongs to. For example, in order to shut down the application under test and to free "
	+ "its resources via the {{$ApplicationStop$}} action the user must pass a valid object to this action, "
	+ "which was received from {{$ApplicationStart$}} action.",
examples				=
	  "As a rule, {{$ApplicationStart$}} is placed in one of the initial TestCases where initialization is performed. "
	+ "Therefore it requires that the {{$Global$}} flag be set, as access to the output value is necessary within "
	+ "the whole matrix rather than just within the {{$TestCase$}} containing the action. "
	+ "{{#\n" +
	  "#Id;#Global;#Action;#AppId;#Browser;#URL\n"
	+ "APPSTR1;1;ApplicationStart;'WEB';Browser;Env1 #}} "
	+ "It is a standart using this action with web.jar plugin.",
seeAlsoClass = {ApplicationStop.class, ApplicationConnectTo.class, ApplicationGetProperties.class, ApplicationNewInstance.class,
ApplicationRefresh.class, ApplicationResize.class, ApplicationSwitchTo.class, DialogAlert.class, DialogCheckLayout.class,
		DialogClose.class, DialogFill.class, DialogSwitchToWindow.class}
)
public class ApplicationStart extends AbstractAction 
{
	public static final String idName 	= "AppId";


	@ActionFieldAttribute(name = idName, mandatory = true, description = 
		  "Adapter key, one of those described in the App entries branch of the configuration, "
		+ "which will be used for starting the corresponding plug-in and selecting the dictionary. "
		+ "The plug-in, in its turn, will start the application under test using the transferred parameters. "
		+ "If no record is found in a configuration for the corresponding {{$AppId$}}, the action will result in an error.")
	protected String id	= null;
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.START, this.owner.getMatrix(), context, parameters, idName, null);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		boolean res = false;
		switch (fieldName)
		{
			case idName:
				res = true;
				break;
				
			default:
				res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, idName, null, fieldName);
				break;
		}	
		
		return res ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case idName:
				Helper.applicationsNames(list, context);
				break;

			default:
				Helper.fillListForParameter(list, this.owner.getMatrix(), context, parameters, idName, null, parameterToFill);
				break;
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		IApplicationPool pool = context.getConfiguration().getApplicationPool();

		Map<String, String> args = new HashMap<>();
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			args.put(parameter.getName(), Str.asString(parameter.getValue()));
		}
		
		AppConnection connection = pool.startApplication(this.id, args);
		if (connection.isGood())
		{
			super.setResult(connection);
		}
		else
		{
			super.setError("Application is not started.", ErrorKind.APPLICATION_ERROR);
		}
	}
}
