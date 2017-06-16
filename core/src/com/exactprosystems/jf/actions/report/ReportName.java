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

@ActionAttribute(
		group					= ActionGroups.Report,
		generalDescription 		= "The following action is needed to add text to the name of the created report.",
		additionFieldsAllowed 	= false,
		examples 				= "If to execute the following example the name of the report file will be given – 'Date and time of"
				+ " running matrix'_'Matrix name'_'PASSED' Chrome.html\n"
				+ "{{##Action;#Name\n"
				+ "ReportName;'Chrome'#}}"
	)
public class ReportName extends AbstractAction 
{
	public final static String nameName = "Name";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "Text that will be added to the name of the report.")
	protected String name 		= "";
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		report.setName(this.name);
		super.setResult(null);
	}
}

