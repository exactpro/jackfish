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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Configuration;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

import java.util.Date;

@ActionAttribute(
		group					   = ActionGroups.Report,
		constantGeneralDescription = R.REPORT_START_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		suffix                     = "REP",
		constantOutputDescription  = R.REPORT_START_OUTPUT_DESC,
        outputType                 = ReportBuilder.class,
		constantExamples 		   = R.REPORT_START_EXAMPLE
	)
public class ReportStart extends AbstractAction 
{
	public static final String reportNameName = "ReportName";
	public static final String versionName    = "Version";

	@ActionFieldAttribute(name = reportNameName, mandatory = true, constantDescription = R.REPORT_START_REPORT_NAME)
	protected String reportName;

	@ActionFieldAttribute(name = versionName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.REPORT_START_VERSION)
	protected String version;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (Str.IsNullOrEmpty(this.reportName))
		{
			super.setError(reportNameName, ErrorKind.EMPTY_PARAMETER);
			return;
		}

		Configuration config = context.getConfiguration();
		ReportBuilder newReport = config.getReportFactory().createReportBuilder(config.getReports().get(), this.reportName, new Date());
		newReport.reportStarted(null, this.version);
		newReport.itemStarted(this.owner.getMatrix().getRoot());

		super.setResult(newReport);
	}
}
