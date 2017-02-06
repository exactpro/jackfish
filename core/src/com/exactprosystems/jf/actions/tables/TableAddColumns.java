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
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is determined to add columns to the table given. It can be used if a "
				+ "table is created from different sources or if new columns are added to the table given.",
		additionFieldsAllowed 	= false,
		seeAlso = "{{@TableReplace@}}, {{@TableColumnRename@}}, {{@AddValue@}}, {{@TableConsiderColumnsAs@}}",
		examples 				=
				"{{`1. Create a table with columns Name and Age.`}}"
				+ "{{`2. Add to the table created columns Gender and Salary (to the line number 0).`}}"
				+ "{{`3. Verify that the table has columns  Gender, Salary, Name and Age. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;;\n"
				+ "#EndRawTable\n"
				+ "\n"
				+ "#Action;#Index;#Table;#Columns\n"
				+ "TableAddColumns;0;TC;{'Gender','Salary'}\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "TC.getHeader(0) == 'Gender' && TC.getHeader(1) == 'Salary' && TC.getHeader(2) == 'Name' && TC.getHeader(3) == 'Age';'Table is not correct'#}}"
	)
public class TableAddColumns extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String columnsName = "Columns";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table which is needed to add columns. ")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = columnsName, mandatory = true, description = "Array of column titles’ names.")
	protected String[]	columns 	= new String[] {};

	@ActionFieldAttribute(name = indexName, mandatory = false, description = "Line number  where it is needed to insert."
			+ "Numeration starts with 0. By default it will be inserted at end of the table.")
	protected Integer	index;
	
	public TableAddColumns()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		index	= Integer.MIN_VALUE;
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


