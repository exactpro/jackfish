////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
    public final static String xmlName            = "Xml";
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String titleName          = "Title";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_REPORT_XML)
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.XML_REPORT_TITLE)
	protected String 	title 	= null;

	@ActionFieldAttribute(name=toReportName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_REPORT_BEFORE_TESTCASE)
	protected String 	beforeTestCase 	= null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.xml.report(report, this.beforeTestCase, Str.asString(this.title));
		
		super.setResult(null);
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
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
			default:
		}
	}
}

