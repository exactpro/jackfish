////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
	public final static String nameName = "Name";
	public final static String failedStepsName = "FailedSteps";
	public final static String passedStepsName = "PassedSteps";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "Text that will be added to the name of the report.")
	protected String name 		= "";

	@ActionFieldAttribute(name = failedStepsName, mandatory = false, description = "Failed steps count that will be added to the report.")
	protected Integer failedSteps;

	@ActionFieldAttribute(name = passedStepsName, mandatory = false, description = "Passed steps count that will be added to the report.")
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

