////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.report;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.File;

@ActionAttribute(
		group					   = ActionGroups.Report,
		constantGeneralDescription = R.REPORT_SHOW_GENERAL_DESC,
		additionFieldsAllowed      = false,
		constantExamples           = R.REPORT_SHOW_EXAMPLE
	)
public class ReportShow extends AbstractAction 
{
	public final static String reportName = "Report";

	@ActionFieldAttribute(name = reportName, mandatory = true, constantDescription = R.REPORT_SHOW_REPORT)
	protected String 	report 	= null;

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
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!new File(this.report).exists())
		{
			setError("File not found", ErrorKind.WRONG_PARAMETERS);
			return;
		}
		context.showReport(this.report);

		super.setResult(null);
	}
}
