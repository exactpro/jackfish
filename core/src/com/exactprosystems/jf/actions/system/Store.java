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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group 					   = ActionGroups.System,
		constantGeneralDescription = R.STORE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.STORE_EXAMPLE,
		seeAlsoClass 			   = {Restore.class}
)

public class Store extends AbstractAction
{
	public final static String nameName = "Name";
	public final static String valueName = "Value";

	@ActionFieldAttribute(name = nameName, mandatory = true, constantDescription = R.STORE_NAME)
	protected String name = null;

	@ActionFieldAttribute(name = valueName, mandatory = false, constantDescription = R.STORE_VALUE)
	protected Object value = null;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getConfiguration().storeGlobal(this.name, this.value);
		
		super.setResult(null);
	}
}
