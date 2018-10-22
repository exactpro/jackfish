/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

