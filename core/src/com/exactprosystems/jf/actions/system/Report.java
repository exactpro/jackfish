////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

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
		group					= ActionGroups.System,
		generalDescription 		= "Reports given string and parameters to the report.",
		additionFieldsAllowed 	= true
	)
public class Report extends AbstractAction 
{
	public final static String strName = "Str";

	@ActionFieldAttribute(name = strName, mandatory = false, description = "Reports given string and parameters to the report.")
	protected String message; 
	
	public Report()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		message 		= ""; 
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
		
		boolean on = report.reportIsOn();
		report.reportSwitch(true);
		ReportTable info = report.addTable(sb.toString(), true, 0, new int[] {});
		info.addValues("");
		report.reportSwitch(on);
		
		
		super.setResult(null);
	}

	@Override
	protected boolean reportAllDetail()
	{
		return false;
	}
}
