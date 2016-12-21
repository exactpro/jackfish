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
		generalDescription 		= "Shows either currnent matrix report or pointed report.",
		additionFieldsAllowed 	= false
	)
public class ReportShow extends AbstractAction 
{
	public final static String reportName = "Report";

	@ActionFieldAttribute(name = reportName, mandatory = true, description = "Report that need to show in report window. "
	        + "If omitted the current matrix report will be shown.")
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
