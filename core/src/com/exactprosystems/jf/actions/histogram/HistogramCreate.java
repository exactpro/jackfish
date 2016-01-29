////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.histogram;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.actions.app.ApplicationHelper;
import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Histogram;

import java.util.List;

@ActionAttribute(
		group = ActionGroups.Histograms,
		suffix = "HST",
		additionFieldsAllowed = false,
		generalDescription = "Create new histogram",
		outputType = Histogram.class)
public class HistogramCreate extends AbstractAction
{
	public static final String appConnectionName = "AppConnection";
	public static final String listenParameterName = "ParameterListen";
	public static final String intervalName = "Interval";
	public static final String intervalCountName = "IntervalCount";

	@ActionFieldAttribute(name = appConnectionName, description = "The application connection", mandatory = true)
	protected AppConnection appConnection = null;

	@ActionFieldAttribute(name = listenParameterName, description = "Parameter which will be listening", mandatory = true)
	protected String parameterKind = null;

	@ActionFieldAttribute(name = intervalName, description = "Interval. In ms. Default value is 50ms", mandatory = false)
	protected Integer interval = 50;

	@ActionFieldAttribute(name = intervalCountName, description = "Count of intervals. Default value is 100", mandatory = false)
	protected Integer intervalCount = 100;

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case listenParameterName:
				if (appConnection != null)
				{
					ApplicationHelper.fillListForParameter(list, context, parameters, appConnection.getId(), listenParameterName);
				}
				break;
		}
	}

	@Override
	protected ActionItem.HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case listenParameterName:
				return ActionItem.HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (appConnection == null)
		{
			throw new NullPointerException(String.format("Field with name '%s' can't be null", appConnectionName));
		}
		if (parameterKind == null)
		{
			throw new NullPointerException(String.format("Field with name '%s' can't be null", listenParameterName));
		}
		Histogram histogram = new Histogram(appConnection, parameterKind, interval, intervalCount);

		setResult(histogram);
	}
}
