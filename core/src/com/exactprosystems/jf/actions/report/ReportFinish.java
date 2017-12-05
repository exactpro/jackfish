////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
