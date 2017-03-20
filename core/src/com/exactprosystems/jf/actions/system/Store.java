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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group = ActionGroups.System,
		generalDescription = "The following action is needed to store values of the object from any matrix in global {{$Store$}}.\n"
				+ "Later, this object is available by action {{@Restore@}}.\n"
				+ "All objects from global {{$Store$}} are stored only during the current session.",
		additionFieldsAllowed = false,
		examples 			= "Save object DateTime that contains current date and time when doing this action.\n"
				+ "{{##Action;#Value;#Name\n"
				+ "Store;DateTime.current();'Current time'#}}",
		seeAlso = "{{@Restore@}}",
		seeAlsoClass = {Restore.class}
)

public class Store extends AbstractAction
{
	public final static String nameName = "Name";
	public final static String valueName = "Value";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "The name of the saved object.")
	protected String name = null;

	@ActionFieldAttribute(name = valueName, mandatory = true, description = "The object that needs to be stored.")
	protected Object value = null;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getConfiguration().storeGlobal(this.name, this.value);
		
		super.setResult(null);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
