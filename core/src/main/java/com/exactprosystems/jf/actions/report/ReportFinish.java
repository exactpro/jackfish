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

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.util.Date;

@ActionAttribute(
		group					   = ActionGroups.Report,
		constantGeneralDescription = R.REPORT_FINISH_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.REPORT_FINISH_EXAMPLE
	)
public class ReportFinish extends AbstractAction 
{
	public static final String passedName     = "Passed";
	public static final String failedName     = "Failed";
	public static final String reportName     = "Report";
	public static final String startTimeName  = "StartTime";
	public static final String finishTimeName = "FinishTime";

	@ActionFieldAttribute(name = reportName, mandatory = true, constantDescription = R.REPORT_FINISH_REPORT)
	protected ReportBuilder report;

	@ActionFieldAttribute(name = passedName, mandatory = true, constantDescription = R.REPORT_FINISH_PASSED)
	protected Integer passed;

	@ActionFieldAttribute(name = failedName, mandatory = true, constantDescription = R.REPORT_FINISH_FAILED)
	protected Integer failed;

	@ActionFieldAttribute(name = startTimeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.REPORT_FINISH_START_TIME)
	protected Date startTime;

	@ActionFieldAttribute(name = finishTimeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.REPORT_FINISH_FINISH_TIME)
	protected Date finishTime;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.report.itemFinished(this.owner.getMatrix().getRoot(), 0, null);
		this.report.reportFinished(this.failed, this.passed, this.startTime, this.finishTime);

		super.setResult(null);
	}
}
