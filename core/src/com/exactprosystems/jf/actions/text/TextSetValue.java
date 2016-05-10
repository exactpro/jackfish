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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					= ActionGroups.Text,
		generalDescription 		= "Sets line of the text.",
		additionFieldsAllowed 	= false
	)
public class TextSetValue extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String lineName = "Line";
	public final static String indexName = "Index";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "The text object.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = lineName, mandatory = true, description = "Contains of line.")
	protected String	line 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = true, description = "Index of line.")
	protected Integer	index 	= 0;

	public TextSetValue()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		text.set(this.index, this.line);
		
		super.setResult(null);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}

