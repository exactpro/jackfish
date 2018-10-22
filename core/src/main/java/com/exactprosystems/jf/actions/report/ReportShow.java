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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.File;

@ActionAttribute(
		group					   = ActionGroups.Report,
		constantGeneralDescription = R.REPORT_SHOW_GENERAL_DESC,
		additionFieldsAllowed      = false,
		constantExamples           = R.REPORT_SHOW_EXAMPLE
	)
public class ReportShow extends AbstractAction 
{
	public static final String reportName = "Report";

	@ActionFieldAttribute(name = reportName, mandatory = true, constantDescription = R.REPORT_SHOW_REPORT)
	protected String report;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (reportName.equals(fieldName))
		{
			return HelpKind.ChooseOpenFile;
		}
		return null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!new File(this.report).exists())
		{
			setError(String.format("Report by path %s not found", this.report), ErrorKind.WRONG_PARAMETERS);
			return;
		}
		context.showReport(this.report);

		super.setResult(null);
	}
}
