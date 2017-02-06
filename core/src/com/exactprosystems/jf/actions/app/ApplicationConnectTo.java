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
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
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
		generalDescription 		= "Plug-in dependent action. The purpose of the action is connect to the running application."
				+ "The action requires some additional parameters which depend on the type of the plug-in used.",
		additionFieldsAllowed 	= true,
		additionalDescription = "The parameters are determined by the chosen plug-in."
				+ "For example, the available parameters for win.jar are the following:"
				+ "{{` {{$Main window$}} - a text string to search for the window of the application to connect to.`}}"
				+ "{{` {{$Height$}} - the height of the window.`}} {{` {{$Width$}} - the width of the window.`}} The parameters can be chosen"
				+ " in the dialogue window opened with the context menu of this action in {{$“All parameters”$}} option.",
		outputDescription 		= "A special object which identifies the started application session."
				+ "This object is required in many other actions to specify the session of the application the"
				+ " indicated action belongs to. Should be created with an active {{$“Global”$}} flag.",
		outputType				= AppConnection.class,
		seeAlso					= "{{@ApplicationStop@}}, {{@ApplicationStart@}}, {{@ApplicationGetProperties@}},"
				+ " {{@ApplicationNewInstance@}}, {{@ApplicationRefresh@}}, {{@ApplicationResize@}}, "
				+ "{{@ApplicationSwitchTo@}}, {{@DialogAlert@}}, {{@DialogCheckLayout@}}, {{@DialogClose@}}, "
				+ "{{@DialogFill@}}, {{@DialogSwitchToWindow@}}",
		examples = "{{##Id;#Global;#Action;#Browser;#URL;#AppId\n" +
				"app;1;ApplicationStart;'Chrome';'http://google.com';'WEB'\n" +
				"\n" +
				"#Assert;#Message\n" +
				"app.Out.IsGood();'Connection is not established'#}}"
	)
public class ApplicationConnectTo extends AbstractAction 
{
	public static final String idName 			= "AppId";


	@ActionFieldAttribute(name = idName, mandatory = true, description = "Adapter key, one of those described in the"
			+ " {{$App entries$}} branch of the configuration, will be used to start the corresponding plug-in and to select"
			+ " the dictionary.")
	protected String 		id	= null;

	public ApplicationConnectTo()
	{
	}

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.CONNECT, this.owner.getMatrix(), context, parameters, idName, null);
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
		
		AppConnection connection = pool.connectToApplication(this.id, args);
		if (connection.isGood())
		{
			super.setResult(connection);
		}
		else
		{
			super.setError("Application is not found.", ErrorKind.APPLICATION_ERROR);
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
