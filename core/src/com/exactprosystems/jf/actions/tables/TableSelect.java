////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.conditions.Condition;
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBLSLCT",
		generalDescription 		= "Sets value in the row of the table.",
		additionFieldsAllowed 	= true,
		outputDescription 		= "Table structure.",
		outputType				= Table.class
	)
public class TableSelect extends AbstractAction 
{
	public final static String tableName = "Table";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	public TableSelect()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters extra = parameters.select(TypeMandatory.Extra);
		Condition[] conditions = Condition.convertToCondition(extra);
		Table newTable = this.table.select(conditions);
		
		super.setResult(newTable);
	}
}

