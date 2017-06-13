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
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.Let;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

@ActionAttribute(
		group 			 	  = ActionGroups.System,
		generalDescription 	  = "{{*Deprecated.*}}\n"
				+ "The following action is needed to create and assigning values to the variables.\n"
				+ "A variable is needed to store objects, numbers, and rows.\n"
				+ "A local variable is available in  {{@TestCase@}}, {{@SubCase@}}, {{@Step@}}. A global variable is available \n"
				+ "in the current matrix.\n"
				+ "A drawback of this action is impossibility to use values of other variables given in the same action.\n"
				+ "See {{@Let@}}.",
		additionFieldsAllowed = true,
		additionalDescription = "The name of the variable is specified in the name of the parameter, the value of the"
				+ " variable – in the value of the parameter. When tagged as 'G' a variable is created as global.\n"
				+ "More than one variable could be given.",
		examples 			  = "Create 2 global variables with names 'name' and 'age', with values 'Mike' and '42' accordingly.\n"
				+ "{{##Global;#Action;#name;#age\n"
				+ "1;Vars;'Mike';'42'#}}",
		seeAlsoClass = {Let.class}
)

@Deprecated
public class Vars extends AbstractAction
{

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (owner.isGlobal())
		{
			evaluator.getGlobals().set(parameters.select(TypeMandatory.Extra));
		}
		else
		{
			evaluator.getLocals().set(parameters.select(TypeMandatory.Extra));
		}
		super.setResult(null);
	}
}
