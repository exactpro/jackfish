////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Messages,
		suffix					   = "MSGCMP",
		constantGeneralDescription = R.MESSAGE_COMPARE_TWO_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.MESSAGE_COMPARE_TWO_EXAMPLE
	)
public class MessageCompareTwo extends AbstractAction 
{
	public static final String actualName   = "Actual";
	public static final String expectedName = "Expected";
	public static final String excludeName  = "Exclude";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.MESSAGE_COMPARE_TWO_ACTUAL)
	protected MapMessage actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.MESSAGE_COMPARE_TWO_EXPECTED)
	protected MapMessage expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_COMPARE_TWO_EXCLUDE)
	protected String[] exclude;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
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
