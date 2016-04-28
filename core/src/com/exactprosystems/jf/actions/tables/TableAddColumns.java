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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "Adds new columns into the table.",
		additionFieldsAllowed 	= false
	)
public class TableAddColumns extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String columnsName = "Columns";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = false, description = "Index in the table")
	protected Integer	index	= Integer.MIN_VALUE;

	@ActionFieldAttribute(name = columnsName, mandatory = true, description = "Array of new columns.")
	protected String[]	columns 	= new String[] {};
	
	public TableAddColumns()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (index >= 0)
		{
			table.addColumns(index, columns);
		}
		else
		{
			table.addColumns(columns);
		}

		super.setResult(null);
	}
}

