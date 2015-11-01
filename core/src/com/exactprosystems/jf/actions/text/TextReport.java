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
		generalDescription 		= "Reports the text to the report.",
		additionFieldsAllowed 	= false
	)
public class TextReport extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String titleName = "Title";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "The text object.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = titleName, mandatory = true, description = "Title.")
	protected String 	title 	= null;

	
	public TextReport()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		text.report(report, this.title);
		
		super.setResult(null);
	}

	@Override
	protected boolean reportAllDetail()
	{
		return false;
	}
}

