////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

import java.util.function.Supplier;

@ActionAttribute(
		group					= ActionGroups.Text,
		generalDescription 		=
 "The purpose of the action is to display the object content {{$Text$}} in the report on the matrix run. "
+ "The object type {{$Text$}} is the text-based pattern which consists of lines. "
+ "It is applied when it is necessary to examine the content of the object {{$Text$}}.",
		additionFieldsAllowed 	= false,
		examples =
 "{{##Id;#Action;#Content\n"
+ "TXT1;TextCreate;'Text'\n"
+ "\n"
+ "\n"
+ "#Action;#Title;#Text\n"
+ "TextReport;’My text’;TXT1.Out#}}\n",
		seeAlso = "{{@TextPerform@}}, {{@TextAddLine@}}, {{@TextLoadFromFile@}}, {{@TextCreate@}}, {{@TextSaveToFile@}}," +
				" {{@TextSetValue@}}"
	)
public class TextReport extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String beforeTestCaseName = "BeforeTestCase";
	public final static String titleName = "Title";
	public final static String	toReportName		= "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = "Rerouting report")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Object {{$Text$}}, which is required to output.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "It accepts id test case before " +
			"which the text will be displayed in the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "The title of the text.")
	protected String 	title 	= null;

	
	public TextReport()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		this.beforeTestCase = null;
		this.toReport = null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Supplier<ReportBuilder> currentReport = () -> this.toReport == null ? report : this.toReport;
		this.text.report(currentReport.get(), this.beforeTestCase, this.title);
		
		super.setResult(null);
	}

	@Override
	protected boolean reportAllDetail()
	{
		return false;
	}
}

