////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					= ActionGroups.XML,
		suffix					= "XMLCMP",
		generalDescription 		= "Compares the given XMLs.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if XMLs are equal.",
		outputType				= Boolean.class
	)
public class XmlCompare extends AbstractAction 
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "One XML object, got from any source.")
	protected Xml actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "Another XML object, got from any source.")
	protected Xml expected = null;

	public XmlCompare()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual XML object is null");
			return;
		}

		if (this.expected == null)
		{
			super.setError("Expected XML object is null");
			return;
		}
	
		boolean res = this.actual.equals(this.expected);
		
		super.setResult(res);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
