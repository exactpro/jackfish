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

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.XML,
		constantGeneralDescription = R.XML_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.XML_REPORT_EXAMPLE
	)
public class XmlReport extends AbstractAction 
{
	public static final String xmlName            = "Xml";
	public static final String beforeTestCaseName = "BeforeTestCase";
	public static final String titleName          = "Title";
	public static final String toReportName       = "ToReport";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_REPORT_XML)
	protected Xml xml;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.XML_REPORT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = toReportName, mandatory = false, constantDescription = R.XML_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, constantDescription = R.XML_REPORT_BEFORE_TESTCASE)
	protected String beforeTestCase;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (beforeTestCaseName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
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
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.xml.report(report, this.beforeTestCase, Str.asString(this.title));

		super.setResult(null);
	}
}

