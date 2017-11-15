////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.app.IRemoteApplication;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@ActionAttribute(
		group 						  = ActionGroups.App,
		suffix 						  = "APPPAR",
		constantGeneralDescription    = R.APP_GET_PROPERTIES_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.APP_GET_PROPERTIES_ADDITIONAL_DESC,
        outputType              	  = Map.class,
        constantOutputDescription 	  = R.APP_GET_PROPERTIES_OUTPUT_DESC,
		constantExamples 			  = R.APP_GET_PROPERTIES_EXAMPLE,
		seeAlsoClass = { ApplicationSetProperties.class, ApplicationStart.class, ApplicationConnectTo.class }
)
public class ApplicationGetProperties extends AbstractAction
{
	public static final String	connectionName	= "AppConnection";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_GET_PROPERTIES_CONNECTION)
	protected AppConnection		connection		= null;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		Helper.helpToAddParameters(list, ParametersKind.GET_PROPERTY, this.owner.getMatrix(), context, parameters, null, connectionName);
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
		Map<String, Object> outValue = new LinkedHashMap<>();
		
		IApplication app = connection.getApplication();
		IRemoteApplication service = app.service();
		
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			if (parameter.getValue() != null && !(parameter instanceof Serializable))
			{
				super.setError("Parameter " + parameter.getName() + " should be Serializable type or empty.", ErrorKind.WRONG_PARAMETERS);
				return;
			}
			Serializable prop = (Serializable) parameter.getValue();
			Serializable value = service.getProperty(parameter.getName(), prop);

			outValue.put(parameter.getName(), value);
		}

		super.setResult(outValue);
	}
}
