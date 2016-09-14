////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.Tables, 
		generalDescription 		= "Adds all rows from a table to the end of main table .", 
		additionFieldsAllowed 	= false 
)
public class TableUnion extends AbstractAction
{
	public final static String mainTableName = "MainTable";
	public final static String unitedTableName = "UnitedTable";

	@ActionFieldAttribute(name = mainTableName, mandatory = true, description = "The table.")
	protected Table mainTable = null;

	@ActionFieldAttribute(name = unitedTableName, mandatory = true, description = "The table.")
	protected Table unitedTable = null;

	public TableUnion()
	{
	}

	@Override
	public void initDefaultValues()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report,
			Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		for (RowTable row : this.unitedTable)
		{
			this.mainTable.add(row);
		}

		super.setResult(null);
	}
}
