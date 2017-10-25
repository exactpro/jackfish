////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplication;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group					      = ActionGroups.App,
		suffix						  = "APPSW",
		constantGeneralDescription    = R.APP_SWITCH_TO_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.APP_SWITCH_TO_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.APP_SWITCH_TO_OUTPUT_DESC,
		outputType					  = String.class,
		constantExamples 			  = R.APP_SWITCH_TO_EXAMPLE,
		seeAlsoClass 				  = {ApplicationStart.class, ApplicationConnectTo.class}
	)
public class ApplicationSwitchTo extends AbstractAction
{
	public final static String connectionName = "AppConnection";
	public final static String softConditionName = "SoftCondition";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_SWITCH_TO_CONNECTION)
	protected AppConnection	connection	= null;

	@ActionFieldAttribute(name = softConditionName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.APPLICATION_SWITCH_TO_SOFT_CONDITION)
	protected Boolean 				softCondition;

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
		if (this.softCondition == null)
		{
			super.setError(softConditionName + " is null", ErrorKind.WRONG_PARAMETERS);
			return;
			
		}
	    Map<String, String> map = new HashMap<>();
	    parameters.select(TypeMandatory.Extra).makeCopy().forEach((k,v) -> map.put(k, String.valueOf(v)));
	    
		IApplication app = this.connection.getApplication();
		String res = app.service().switchTo(map, this.softCondition);
		
		if (res.equals(""))
		{
			super.setError("Can not find the window.", ErrorKind.ELEMENT_NOT_FOUND);
		}
		else
		{
			super.setResult(res);
		}
	}



}
