////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import java.io.Serializable;
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
		generalDescription 	= "Plug-in dependent action. The purpose of the action is to set certain properties"
				+ " into the available connection.",
		additionFieldsAllowed = true,
		additionalDescription = "The parameters are determined by the chosen plug-in. {{`For example, additional "
				+ "parameters {{$CookieAdd$}} and {{$CookieRemove$}} are available for web plug-in. They are necessary "
				+ "to add and to remove a cookie respectively.`}} The parameters can be chosen in the dialogue"
				+ " window opened with the context menu of this action in {{$“All parameters”$}} option.",
		examples = "{{##Id;#Action;#CookieRemove\n"
				+ "AGP1;ApplicationGetProperties;'name'\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "AGP1.Out.CookieRemove;'Cookie was not removed'#}}",
		seeAlsoClass = { ApplicationGetProperties.class, ApplicationStart.class, ApplicationConnectTo.class }
)
public class ApplicationSetProperties extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, description = "A special object which identifies"
			+ " the started application session. This object is required in many other actions to specify the session"
			+ " of the application the indicated action belongs to."
			+ "It is the output value of such actions as {{@ApplicationStart@}}, {{@ApplicationConnectTo@}}.")
	protected AppConnection		connection		= null;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.SET_PROPERTY, this.owner.getMatrix(), context, parameters, null, connectionName);
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
		IApplication app = connection.getApplication();
		IRemoteApplication service = app.service();
		
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
		    if (parameter.getValue() instanceof Serializable)
		    {
		        Serializable prop = (Serializable)parameter.getValue();
    			service.setProperty(parameter.getName(), prop);
		    }
		    else
		    {
		        super.setError("Parameter " + parameter.getName() + " should be Serializable type.", ErrorKind.WRONG_PARAMETERS);
		        return;
		    }
		}
		super.setResult(null);
	}
}
