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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;

@ActionAttribute(
		group = ActionGroups.System,
		generalDescription = "Store value of global object. That object can be accessed from any matrixes.",
		additionFieldsAllowed = false
)

public class Store extends AbstractAction
{
	public final static String nameName = "Name";
	public final static String valueName = "Value";

	@ActionFieldAttribute(name = nameName, mandatory = true, description = "Name of a global storage object.")
	protected String name = null;

	@ActionFieldAttribute(name = valueName, mandatory = true, description = "Value of a global storage object.")
	protected Object value = null;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getConfiguration().storeGlobal(this.name, this.value);
		
		super.setResult(null);
	}
}
