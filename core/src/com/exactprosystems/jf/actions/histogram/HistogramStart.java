////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2016, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////
package com.exactprosystems.jf.actions.histogram;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Histogram;

@ActionAttribute(
		group = ActionGroups.Histograms,
		additionFieldsAllowed = false,
		generalDescription = "Start listen events")
public class HistogramStart extends AbstractAction
{
	public static final String histogramName = "Histogram";

	@ActionFieldAttribute(name = histogramName, description = "Histogram", mandatory = true)
	protected Histogram histogram = null;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (histogram == null)
		{
			throw new NullPointerException("Field 'Histogram' can't be null");
		}
		this.histogram.start();
		setResult(null);
	}
}
