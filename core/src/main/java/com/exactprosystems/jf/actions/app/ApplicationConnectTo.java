/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.actions.gui.*;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.common.ParametersKind;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@ActionAttribute(
		group						  = ActionGroups.App,
		suffix						  = "APPSTR",
		constantGeneralDescription    = R.APP_CONNECT_TO_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.APP_CONNECT_TO_ADDITIONAL_DESC,
		constantOutputDescription 	  = R.APP_CONNECT_TO_OUTPUT_DESC,
		outputType					  = AppConnection.class,
		constantExamples 			  = R.APP_CONNECT_TO_EXAMPLE,
		seeAlsoClass 				  = {ApplicationStop.class, ApplicationStart.class, ApplicationGetProperties.class,
				ApplicationNewInstance.class, ApplicationRefresh.class,	ApplicationResize.class, ApplicationSwitchTo.class,
				DialogAlert.class, DialogCheckLayout.class, DialogClose.class, DialogFill.class, DialogSwitchToWindow.class}
	)
public class ApplicationConnectTo extends AbstractAction 
{
	public static final String idName         = "AppId";
	public static final String connectionName = "AppConnection";

	@ActionFieldAttribute(name = idName, mandatory = true, constantDescription = R.APPLICATION_CONNECT_TO_ID)
	protected String id = null;

	@ActionFieldAttribute(name = connectionName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.APPLICATION_CONNECT_TO_CONNECTION)
	protected AppConnection connection;

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

			case connectionName:
				res = false;
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

		Map<String, String> args = parameters.select(TypeMandatory.Extra)
				.stream()
				.collect(Collectors.toMap(Parameter::getName, par -> Str.asString(par.getValue())));

		AppConnection appConnection;
		if (this.connection == null)
		{
			appConnection = pool.connectToApplication(this.id, args);
		}
		else
		{
			appConnection = this.connection;
			pool.reconnectToApplication(appConnection, args);
		}

		if (appConnection.isGood())
		{
			super.setResult(appConnection);
		}
		else
		{
			super.setError("Application is not found.", ErrorKind.APPLICATION_ERROR);
		}
	}
}
