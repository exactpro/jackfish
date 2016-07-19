////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.ErrorKind;

@ActionAttribute(
		group					= ActionGroups.Messages,
		suffix					= "MSGCMP",
		generalDescription 		= "Compares the given messages. It will fail if the messages are not equal.",
		additionFieldsAllowed 	= false
	)
public class MessageCompareTwo extends AbstractAction 
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";
	public final static String excludeName = "Exclude";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "Actual message, got from any source.")
	protected MapMessage actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "Expected message, got from any source.")
	protected MapMessage expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, description = "Fields that will not be compare.")
	protected String[] exclude;
	
	public MessageCompareTwo()
	{
	}

	@Override
	public void initDefaultValues() {
		exclude = null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual message is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if (this.expected == null)
		{
			super.setError("Expected message is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		boolean res = this.actual.extendEquals(this.expected, this.exclude);
		if (res)
		{
			super.setResult(null);
		}
		else
		{
			super.setError("Messages are not equal.", ErrorKind.NOT_EQUAL);
		}
	}
}
