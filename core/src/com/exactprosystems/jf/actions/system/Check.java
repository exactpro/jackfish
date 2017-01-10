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

import java.util.Collections;
import java.util.Map;
import java.util.Map.Entry;

@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "The following action is needed to check structured objects that implement interface"
				+ " Map, for example: table rows, MapMessages.",
		additionFieldsAllowed 	= true,
		outputType				= Boolean.class,
		outputDescription		= "A logical variable, true if matching values are equal, else – false. If there are"
				+ " differences between matching values table {{$Mismatched$}} fields that contains information about "
				+ "mismatched values, is added to the report.",
		additionalDescription = "Helps to pass values and their names that are needed to be compared.",
		examples = "{{`1. Make a table with 2 rows and columns, add values.`}}"
				+ "2. Compare values from the first row of the table with those ones that are specified in additional parameters of action Check."
				+ "{{##Id;#RawTable\n"
				+ "DATA1;Table\n"
				+ "@;Country;Capital\n"
				+ "0;Russia;Moscow\n"
				+ "1;Germany;Berlin\n"
				+ "#EndRawTable\n"
				+ "\n"
				+ "\n"
				+ "#Action;#DoNotFail;#Actual;#Country;#Capital\n"
				+ "Check;true;DATA1.get(0);'Russia';'Berlin'#}}"
	)
public class Check extends AbstractAction 
{
	public final static String dontFailName = "DoNotFail";
	public final static String actualName = "Actual";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "A set of names and values that should be compared.")
	protected Map<String, Object> actual = Collections.emptyMap(); 
	
	@ActionFieldAttribute(name = dontFailName, mandatory = false, description = "If true, then when identifying"
			+ " differences as a result of the comparison, action will still be marked as Passed, otherwise, as "
			+ "Failed. By default, a parameter has a false value.")
	protected Boolean dontFail; 

	public Check()
	{
		dontFail = false;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, String> diff = ClientHelper.difference(new MapMessage(this.actual, null), Condition.convertToCondition(parameters.select(TypeMandatory.Extra))); 
		
		if (diff == null)
		{
			super.setResult(true);
		}
		else
		{
			ReportTable table = report.addTable("Mismatched fields:", null, true, 1, new int[] { 20, 80 }, "Name", "Expected + Actual");
			for (Entry<String, String> entry : diff.entrySet())
			{
				table.addValues(entry.getKey(), entry.getValue());
			}

			if (this.dontFail)
			{
				super.setResult(false);
			}
			else
			{
				super.setError("Object does not match.", ErrorKind.NOT_EQUAL);
			}
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
