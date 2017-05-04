////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.util.List;
import java.util.Map.Entry;

@ActionAttribute(
		group = ActionGroups.System,
		generalDescription 		= "The following action is needed to get the value that was previously saved in global Store (see action {{@Store@}}).\n"
				+ "One can restore only those objects that are in global Store (View -> Store).\n"
				+ "All objects from the global Store are saved only during current tool session.",
		additionFieldsAllowed = false,
		outputDescription 		= "A value that will be required. Otherwise, null, if a non-existent value was required.",
		outputType				= Object.class,
		examples 				= "Restore value with name Current time that was stored by action Store. Assign it to "
				+ "the local variable “create date”.\n"
				+ "{{##Action;#AsVar;#Name\n"
				+ "Restore;'create date';'Current time'#}}",
		seeAlsoClass = {Store.class}
)

public class Restore extends AbstractAction
{
	public final static String nameName = "Name";
	public final static String asVarName = "AsVar";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "The name of the object which value will be restored.")
	protected String name = null;

	@ActionFieldAttribute(name = asVarName, mandatory = false, description = "the name of the variable that will be "
			+ "given the value of the restored object. A variable will be global if an action is tagged “G”, otherwise, variable will be local.")
	protected String asVar;

	@Override
	public void initDefaultValues() 
	{
		asVar = null;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return nameName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}
	
	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		AbstractEvaluator evaluator = context.getEvaluator();
		for(Entry<String, Object> entry : context.getConfiguration().getStoreMap().entrySet())
		{
			list.add(new ReadableValue(evaluator.createString(entry.getKey())));
		}
	}
	
	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Object res = context.getConfiguration().restoreGlobal(this.name);
		if (asVar != null)
		{
			if (owner.isGlobal())
			{
				context.getEvaluator().getGlobals().set(this.asVar, res);
			}
			else
			{
				context.getEvaluator().getLocals().set(this.asVar, res);
			}
		}
		super.setResult(res);
	}
}
