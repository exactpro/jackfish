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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

@ActionAttribute(
		group						  = ActionGroups.System,
		constantGeneralDescription    = R.PRINT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.PRINT_ADDITIONAL_DESC,
		constantExamples      		  = R.PRINT_EXAMPLE,
		seeAlsoClass 				  = {Show.class}
	)
public class Print extends AbstractAction 
{
	public final static String strName = "Str";

	@ActionFieldAttribute(name = strName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.PRINT_MESSAGE)
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
		
		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			sb.append(parameter.getName());
			sb.append(" = ");
			sb.append(parameter.getValue());
			sb.append('\t');
		}
		context.getOut().println(sb.toString());
		
		super.setResult(null);
	}
}
