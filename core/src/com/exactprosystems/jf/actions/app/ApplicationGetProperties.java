////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group 				= ActionGroups.App, 
		suffix 				= "APPPAR", 
		generalDescription 	= "Plug-in dependent action. The purpose of the action is to get certain properties"
				+ " from the available connection.",
		additionFieldsAllowed = true,
		additionalDescription = "The parameters are determined by the chosen plug-in. {{`For example, additional "
				+ "parameters {{$Title$}} and {{$URL$}} are available for web plug-in. They are necessary to get the information"
				+ " about the title bar and the address respectively.`}} The parameters can be chosen in the dialogue"
				+ " window opened with the context menu of this action in {{$“All parameters”$}} option.",
		examples = "{{##Id;#Action;#URL;#AppConnection\n" +
				"AGP1;ApplicationGetProperties;;app\n" +
				"\n" +
				"\n" +
				"#Assert;#Message\n" +
				"!Str.IsNullOrEmpty(AGP1.Out.URL);'String is null or empty'#}} \n",
		seeAlso					=
				"{{@ApplicationStart@}}, {{@ApplicationConnectTo@}}"
)
public class ApplicationGetProperties extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies"
			+ " the started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to."
			+ "It is the output value of such actions as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection		connection		= null;

	public ApplicationGetProperties()
	{
	}
	
	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.PROPERTY, this.owner.getMatrix(), context, parameters, null, connectionName);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		boolean res = Helper.canFillParameter(this.owner.getMatrix(), context, parameters, null, connectionName, fieldName);
		return res ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		Helper.fillListForParameter(list, this.owner.getMatrix(), context, parameters, null, connectionName, parameterToFill);
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
			Map<String, Object> outValue = new LinkedHashMap<>();
			
			IApplication app = connection.getApplication();
			IRemoteApplication service = app.service();
			
			for (Parameter parameter : parameters.select(TypeMandatory.Extra))
			{
				String value = service.getProperty(parameter.getName());
				outValue.put(parameter.getName(), value);
			}

			super.setResult(outValue);
		}
	}

	@Override
	public void initDefaultValues()
	{
	}
}
