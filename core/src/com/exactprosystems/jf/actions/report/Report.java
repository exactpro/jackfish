////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

@ActionAttribute(
		group					= ActionGroups.Report,
		generalDescription 		= "Reports given string and parameters to the report.",
		additionFieldsAllowed 	= true
	)
public class Report extends AbstractAction 
{
    public final static String beforeTestCaseName = "BeforeTestCase";
    public final static String strName            = "Str";
    public final static String toReportName       = "ToReport";

	@ActionFieldAttribute(name=toReportName, mandatory = false, description = 
            "This parameter is used for directing the output from the given object to the external report "
          + "created by the {{$ReportStart$}} action.")
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, description = "The name of Testcase before witch the table will be put.")
	protected String 	beforeTestCase 	= null;

	@ActionFieldAttribute(name = strName, mandatory = false, description = "Reports given string and parameters to the report.")
	protected String message; 
	
	public Report()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		this.message 		= "";
		this.beforeTestCase = null;
		this.toReport = null;
	}
	
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
//		report.outLine(this.owner, this.beforeTestCase, sb.toString(), null);
		
		ReportTable info = report.addTable(sb.toString(), this.beforeTestCase, true, 0, new int[] {});
		info.addValues("");
		
		
		super.setResult(null);
	}
}
