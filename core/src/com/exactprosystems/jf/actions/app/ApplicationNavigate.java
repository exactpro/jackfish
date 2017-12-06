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
import com.exactprosystems.jf.api.app.NavigateKind;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group 					      = ActionGroups.App,
		suffix 						  = "APPPNVG",
		constantGeneralDescription 	  = R.APP_NAVIGATE_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.APP_NAVIGATE_ADDITIONAL_DESC,
		constantExamples 			  = R.APP_NAVIGATE_EXAMPLE,
		seeAlsoClass 				  = {ApplicationStart.class, ApplicationConnectTo.class}
)
public class ApplicationNavigate extends AbstractAction
{
	public static final String connectionName   = "AppConnection";
	public static final String navigateKindName = "Navigate";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_NAVIGATE_CONNECTION)
	protected AppConnection connection;

	@ActionFieldAttribute(name = navigateKindName, mandatory = true, constantDescription = R.APPLICATION_NAVIGATE_KIND)
	protected NavigateKind kind;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		list.add(new ReadableValue(navigateKindName, "Kind to navigation"));
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (navigateKindName.equals(parameterToFill))
		{
			list.addAll(CommonHelper.convertEnumsToReadableList(NavigateKind.values()));
		}
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (navigateKindName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.connection.getApplication().service().navigate(this.kind);
		super.setResult(null);
	}
}
