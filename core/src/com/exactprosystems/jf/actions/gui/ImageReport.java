////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.gui;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
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
	public final static String	imageName	= "Image";
	public final static String  beforeTestCaseName = "BeforeTestCase";
	public final static String	titleName	= "Title";

	public final static String	toReportName		= "ToReport";
	public final static String	asLinkName		= "AsLink";

    @ActionFieldAttribute(name = imageName, mandatory = true, constantDescription = R.IMAGE_REPORT_IMAGE)
    protected ImageWrapper      image;

	@ActionFieldAttribute(name=toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.IMAGE_REPORT_BEFORE_TESTCASE)
	protected String 	beforeTestCase;

	@ActionFieldAttribute(name = titleName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.IMAGE_REPORT_TITLE)
	protected String			title;

	@ActionFieldAttribute(name = asLinkName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.IMAGE_REPORT_AS_LINK)
	protected Boolean			asLink;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		if (this.image == null)
		{
		    super.setError(imageName, ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		report.outImage(super.owner, this.beforeTestCase, this.image.getName(report.getReportDir()), null, Str.asString(this.title), -1,
		        this.asLink ? ImageReportMode.AsLink : ImageReportMode.AsImage);
		super.setResult(this.image.getFileName());
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
				return HelpKind.ChooseFromList;
			case asLinkName:
				return HelpKind.ChooseFromList;
		}

		return null;
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

}
