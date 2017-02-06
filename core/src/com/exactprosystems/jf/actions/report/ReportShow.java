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
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

@ActionAttribute(
		group					= ActionGroups.Report,
		generalDescription 		= "This action is used for Report object display which was finished with ReportFinish action."
				+ " Report will be displayed straight after finishing this action.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Create an object of Report type.`}}"
				+ "{{`2. Create an object of Table type.`}}"
				+ "{{`3. Move the object Table using TableReport action to the report created in the first step.`}}"
				+ "{{`4. Finish building Report object.`}}"
				+ "{{`5. Display the report.`}} "
				+ "{{##Id;#Action;#Version;#ReportName\n"
				+ "REP1;ReportStart;'3.141592';'MyReport'\n"
				+ "\n"
				+ "#Id;#RawTable\n"
				+ "DATA1;Table\n"
				+ "@;newH\n"
				+ "0;newR\n"
				+ "#EndRawTable\n"
				+ "\n"
				+ "#Action;#ToReport;#Table;#Title\n"
				+ "TableReport;REP1.Out;DATA1;'Report title'\n"
				+ "\n"
				+ "#Action;#Passed;#Report;#Failed\n"
				+ "ReportFinish;23;REP1.Out;34\n"
				+ "\n"
				+ "#Action;#Report\n"
				+ "ReportShow;REP1.Out.getReportName()#}}"
	)
public class ReportShow extends AbstractAction 
{
	public final static String reportName = "Report";

	@ActionFieldAttribute(name = reportName, mandatory = true, description = "Object of Report type which should be displayed.")
	protected String 	report 	= null;

	public ReportShow()
	{
	}
	
    @Override
    protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
    {
        switch (fieldName)
        {
            case reportName:
                return HelpKind.ChooseOpenFile;
        }
        return null;
    }
	
	@Override
	public void initDefaultValues() 
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
	    context.showReport(this.report);
		
		super.setResult(null);
	}
}
