/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Report,
		constantGeneralDescription = R.REPORT_NAME_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.REPORT_NAME_EXAMPLE
	)
public class ReportName extends AbstractAction 
{
	public static final String nameName        = "Name";
	public static final String failedStepsName = "FailedSteps";
	public static final String passedStepsName = "PassedSteps";

	@ActionFieldAttribute(name = nameName, mandatory = true, constantDescription = R.REPORT_NAME_NAME)
	protected String name;

	@ActionFieldAttribute(name = failedStepsName, mandatory = false, constantDescription = R.REPORT_NAME_FAILED_STEPS)
	protected Integer failedSteps;

	@ActionFieldAttribute(name = passedStepsName, mandatory = false, constantDescription = R.REPORT_NAME_PASSED_STEPS)
	protected Integer passedSteps;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		report.setName(this.name);
		if (this.failedSteps != null && this.passedSteps != null)
		{
			report.steps(this.failedSteps, this.passedSteps);
		}
		super.setResult(null);
	}
}

