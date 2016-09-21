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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;

@Deprecated
@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "Test action only. Sets output, result and reason from its parameters",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Output is equal ExpectedOut",
		outputType				= Object.class
	)
public class TestAction extends AbstractAction 
{
	public final static String expectedOutName 		= "ExpectedOut";
	public final static String expectedResultName 	= "ExpectedResult";
	public final static String expectedReasonName 	= "ExpectedReason";

	@ActionFieldAttribute(name = expectedOutName, mandatory = false, description = "expected output value.")
	protected Object expectedOut;

	@ActionFieldAttribute(name = expectedResultName, mandatory = false, description = "expected result value.")
	protected String expectedResult;

	@ActionFieldAttribute(name = expectedReasonName, mandatory = false, description = "expected reason value.")
	protected String expectedReason; 
	
	public TestAction()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		expectedOut 		= null;
		expectedResult 		= "" + Result.Passed;
		expectedReason 		= "no reason";
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Result expectedResultEnum = Result.valueOf(this.expectedResult);
		
		if (expectedResultEnum == Result.Passed)
		{
			super.setResult(this.expectedOut);
		}
		else
		{
			super.setError(this.expectedReason, ErrorKind.OTHER);
		}
	}
}
