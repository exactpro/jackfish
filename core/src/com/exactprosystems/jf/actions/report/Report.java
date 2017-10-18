////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.*;
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
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String strName            = "Str";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, def = DefaultValuePool.Null, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{@ReportStart@}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, def = DefaultValuePool.Null, description = "Allows to output a table at the top of the report.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = strName, mandatory = false, def = DefaultValuePool.EmptyString, description = "Output row.")
	protected String message; 
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		StringBuilder sb 	= new StringBuilder();
		if (!message.isEmpty())
		{
			sb.append(message);
			sb.append('\t');
		}
		
		for (Parameter param : parameters.select(TypeMandatory.Extra))
		{
			sb.append(param.getName());
			sb.append(" = ");
			sb.append(param.getValue());
			sb.append('\t');
		}
		
		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		ReportTable info = report.addExplicitTable(sb.toString(), this.beforeTestCase, true, true, new int[] {});
		info.addValues("");
		
		
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
