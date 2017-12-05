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
	protected String beforeTestCase = null;

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
