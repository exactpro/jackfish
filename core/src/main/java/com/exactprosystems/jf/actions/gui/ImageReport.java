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

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportBuilder.ImageReportMode;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;


@ActionAttribute(
		group					   = ActionGroups.GUI,
		suffix					   = "IMGRPT",
		constantGeneralDescription = R.IMAGE_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.IMAGE_REPORT_OUTPUT_DESC,
		outputType 				   = String.class,
		constantExamples 		   = R.IMAGE_REPORT_EXAMPLE
	)
public class ImageReport extends AbstractAction
{
	public static final String imageName          = "Image";
	public static final String beforeTestCaseName = "BeforeTestCase";
	public static final String titleName          = "Title";
	public static final String toReportName       = "ToReport";
	public static final String asLinkName         = "AsLink";

	@ActionFieldAttribute(name = imageName, mandatory = true, constantDescription = R.IMAGE_REPORT_IMAGE)
	protected ImageWrapper image;

	@ActionFieldAttribute(name = toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_REPORT_BEFORE_TESTCASE)
	protected String beforeTestCase;

	@ActionFieldAttribute(name = titleName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.IMAGE_REPORT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = asLinkName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.IMAGE_REPORT_AS_LINK)
	protected Boolean asLink;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
				return HelpKind.ChooseFromList;
			case asLinkName:
				return HelpKind.ChooseFromList;
			default:
				return null;
		}
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case beforeTestCaseName:
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(),  list, context.getEvaluator());
				break;
			case asLinkName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		report.outImage(super.owner, this.beforeTestCase, this.image.getName(report.getReportDir()), null, Str.asString(this.title), -1,
				this.asLink ? ImageReportMode.AsLink : ImageReportMode.AsImage);
		super.setResult(this.image.getFileName());
	}

}
