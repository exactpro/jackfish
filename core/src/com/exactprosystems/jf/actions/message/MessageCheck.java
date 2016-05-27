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
import com.exactprosystems.jf.api.client.ClientHelper;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
		group					= ActionGroups.Messages,
		suffix					= "MSGCHK",
		generalDescription 		= "Compares the given message to a set of name-value pairs.",
		additionFieldsAllowed 	= true
	)
public class MessageCheck extends AbstractAction 
{
	public final static String actualName = "ActualMessage";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "Actual value. It is Message object that was found by GetMessage action. For example FoundOrder.Out")
	protected MapMessage actual = null;

	public MessageCheck()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual object is null");
			return;
		}
		
		Map<String, String> diff = ClientHelper.difference(this.actual, Condition.convertToCondition(parameters.select(TypeMandatory.Extra))); 
		
		if (diff == null)
		{
			super.setResult(null);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:",  1, new int[] { 20, 80 }, "Name", "Expected + Actual");
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}
			
			super.setError("The message does not match.");
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
