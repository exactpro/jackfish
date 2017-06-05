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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					= ActionGroups.Messages,
		suffix					= "MSGCMP",
		generalDescription 		= "The purpose of the action is to compare two MapMessage.\n" +
				"MapMessage is the output of such actions as MessageCreate, ClientCreateMapMessage, ClientDecode, ClientGetMessage, ClientSendMessage. \n" +
				"If there is dissimilarity in the compared objects MapMessage action fails.",
		additionFieldsAllowed 	= false,
		examples = "1-2. Create two objects of MapMessage type with different values \n" +
				"3. Check them with MessageCompareTwo action.\n" +
				"{{##Id;#Action;#Fields\n" +
				"MSGCR1;MessageCreate;{'First item' : 'First Value', 'Second Item' : 'Second Value'}\n" +
				"#Id;#Action;#Fields\n" +
				"MSGCR2;MessageCreate;{'First item' : 'First Value', 'Fourth Item' : 'Fourth Value'}\n" +
				"#Id;#Action;#Expected;#Actual\n" +
				"MSGCMP1;MessageCompareTwo;MSGCR2.Out;MSGCR1.Out#}}"
	)
public class MessageCompareTwo extends AbstractAction 
{
	public final static String actualName = "Actual";
	public final static String expectedName = "Expected";
	public final static String excludeName = "Exclude";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "MapMessage which should be compared.")
	protected MapMessage actual = null;

	@ActionFieldAttribute(name = expectedName, mandatory = true, description = "MapMessage which should be compared with.")
	protected MapMessage expected = null;

	@ActionFieldAttribute(name = excludeName, mandatory = false, def = DefaultValuePool.Null, description = "An array of column names which should be excluded from comparison.")
	protected String[] exclude;
	
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
