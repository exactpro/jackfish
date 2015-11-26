////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.List;
import java.util.Map.Entry;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

@ActionAttribute(
		group = ActionGroups.System,
		generalDescription = "Restore value of global object.",
		additionFieldsAllowed = false,
		outputDescription 		= "Value of global object with the name.",
		outputType				= Object.class
)

public class Restore extends AbstractAction
{
	public final static String nameName = "Name";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "Name of a global storage object.")
	protected String name = null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return nameName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		AbstractEvaluator evaluator = context.getConfiguration().getEvaluator();
		for(Entry<String, Object> entry : context.getConfiguration().getStoreMap().entrySet())
		{
			list.add(new ReadableValue(evaluator.createString(entry.getKey())));
		}
	}
	
	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Object res = context.getConfiguration().restoreGlobal(this.name);
		
		super.setResult(res);
	}
}
