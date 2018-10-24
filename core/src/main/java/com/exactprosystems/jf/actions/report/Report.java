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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;

@ActionAttribute(
		group					      = ActionGroups.Report,
		constantGeneralDescription    = R.REPORT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.REPORT_ADDITIONAL_DESC,
		constantExamples 			  = R.REPORT_EXAMPLE
	)
public class Report extends AbstractAction 
{
	public static final String beforeTestCaseName = "BeforeTestCase";
	public static final String strName            = "Str";
	public static final String toReportName       = "ToReport";

	@ActionFieldAttribute(name = toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.REPORT_BEFORE_TEST_CASE)
	protected String beforeTestCase;

	@ActionFieldAttribute(name = strName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.REPORT_MESSAGE)
	protected String message;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (beforeTestCaseName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (beforeTestCaseName.equals(parameterToFill))
		{
			ActionsReportHelper.fillListForParameter(super.owner.getMatrix(), list, context.getEvaluator());
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		if (!Str.IsNullOrEmpty(this.message))
		{
			sb.append(message);
			sb.append('\t');
		}

		for (Parameter param : parameters.select(TypeMandatory.Extra))
		{
			sb.append(param.getName())
					.append(" = ")
					.append(param.getValue())
					.append('\t');
		}

		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		ReportTable info = report.addExplicitTable(sb.toString(), this.beforeTestCase, true, true, new int[]{});
		info.addValues("");

		super.setResult(null);
	}
}
