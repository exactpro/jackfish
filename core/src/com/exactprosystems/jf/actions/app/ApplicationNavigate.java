////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.app;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.stream.Stream;

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
	public static final String connectionName = "AppConnection";
	public static final String navigateKindName = "Navigate";

	@ActionFieldAttribute(name = connectionName, mandatory = true, constantDescription = R.APPLICATION_NAVIGATE_CONNECTION)
	protected AppConnection connection = null;

	@ActionFieldAttribute(name = navigateKindName, mandatory = true, constantDescription = R.APPLICATION_NAVIGATE_KIND)
	protected NavigateKind kind = null;

	@Override
	protected void helpToAddParametersDerived(List<ReadableValue> list, Context context, Parameters parameters) throws Exception
	{
		list.add(new ReadableValue(navigateKindName, "Kind to navigation"));
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case navigateKindName:
				Stream.of(NavigateKind.values())
						.map(kind -> NavigateKind.class.getSimpleName() + "." + kind.name())
						.map(ReadableValue::new)
						.forEach(list::add);
				break;
			default:
				break;
		}
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case navigateKindName:
				return HelpKind.ChooseFromList;

			default:
				return null;
		}

	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		IApplication app = this.connection.getApplication();
		IRemoteApplication service = app.service();
		service.navigate(this.kind);
		super.setResult(null);
	}
}
