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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					= ActionGroups.Text,
		suffix					= "TXT",
		generalDescription 		= "Perform macro substitutions and return new result Text object.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Text object.",
		outputType				= Text.class
	)
public class TextPerform extends AbstractAction 
{
	public final static String textName = "Text";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "The template text.")
	protected Text 	text 	= null;

	public TextPerform()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Text result = text.perform(evaluator);
		
		super.setResult(result);
	}

}

