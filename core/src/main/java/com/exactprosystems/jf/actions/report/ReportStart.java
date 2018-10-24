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
		Configuration config = context.getConfiguration();
		ReportBuilder newReport = config.getReportFactory().createReportBuilder(config.getReports().get(), this.reportName, new Date());
		newReport.reportStarted(null, this.version);
		newReport.itemStarted(this.owner.getMatrix().getRoot());

		super.setResult(newReport);
	}
}
