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
		generalDescription 		= "Compares the given map of fields to a set of name-value pairs.",
		additionFieldsAllowed 	= true,
		outputType				= Boolean.class,
		outputDescription		= "It is true if the check passes and false otherwise."
	)
public class Check extends AbstractAction 
{
	public final static String dontFailName = "DoNotFail";
	public final static String actualName = "Actual";

	@ActionFieldAttribute(name = actualName, mandatory = true, description = "Actual map of fields.")
	protected Map<String, Object> actual = Collections.emptyMap(); 
	
	@ActionFieldAttribute(name = dontFailName, mandatory = false, description = "If it is true then the action will not be failed anyway. "
																			+ "The action only will return of checking resul.")
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
			ReportTable table = report.addTable("Mismatched fields:", true, 1, new int[] { 20, 80 }, "Name", "Expected + Actual");
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
				super.setError("Object does not match.");
			}
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
