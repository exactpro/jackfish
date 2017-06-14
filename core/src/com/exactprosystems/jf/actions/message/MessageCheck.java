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
import com.exactprosystems.jf.api.error.ErrorKind;
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
		generalDescription 		= "The purpose of the action is to compare MapMessage with the collection of key-value type.\n" +
				"MapMessage is the output of such actions as MessageCreate, ClientCreateMapMessage, ClientDecode, ClientGetMessage, ClientSendMessage. \n" +
				" The report will have a table with 'Mismatched fields' heading, containing columns 'Name' and 'Expected + Actual'.\n" +
				"If there is dissimilarity in the compared objects MapMessage action fails.",
		additionalDescription = "The names and values which should be compared in MapMessage passed in ActualMessage are specified in additional parameters.",
		additionFieldsAllowed 	= true,
		examples = "1. Create an object of MapMessage type.\n" +
				"2. Compare the values in MapMessage with the value passed in Additional parameters. \n" +
				"{{#\n" +
				"#Id;#Action;#Fields\n" +
				"MSGCR1;MessageCreate;{'First item':'First Value', 'Second Item':'Second Value'}\n" +
				"#Id;#Action;#ActualMessage;#First item\n" +
				"MSGCHK1;MessageCheck;MSGCR1.Out;'Second Value'#}}"
	)
public class MessageCheck extends AbstractAction 
{
	public final static String actualName = "ActualMessage";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "an object of MapMessage type.")
	protected MapMessage actual = null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.actual == null)
		{
			super.setError("Actual object is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		
		Map<String, String> diff = ClientHelper.difference(this.actual, Condition.convertToCondition(parameters.select(TypeMandatory.Extra))); 
		
		if (diff == null)
		{
			super.setResult(null);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, true, new int[] { 20, 80 }, "Name", "Expected + Actual");
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}
			
			super.setError("The message does not match.", ErrorKind.NOT_EQUAL);
		}
	}
}
