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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.error.ErrorKind;
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
		examples 				=
				"{{`1. Create a table with column Name.`}}"
				+ "{{`2. Add to the table created column Age (to the line number 0).`}}"
				+ "{{`3. Verify that the table has columns Name and Age. `}}"
				+ "{{#\n#Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name\n"
				+ "0;\n"
				+ "#EndRawTable\n"
				+ "#Action;#Index;#Table;#Columns\n"
				+ "TableAddColumns;0;TC;{'Age'}\n"
				+ "#Assert;#Message\n"
				+ "TC.getHeader(0) == 'Name' && TC.getHeader(1) == 'Age';'Table is not correct'#}}",
		seeAlsoClass = {TableReplace.class, TableColumnRename.class, TableAddValue.class, TableConsiderColumnsAs.class}
	)
public class TableAddColumns extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String columnsName = "Columns";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table which is needed to add columns. ")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = columnsName, mandatory = true, description = "Array of column titles' names.")
	protected String[]	columns 	= new String[] {};

	@ActionFieldAttribute(name = indexName, mandatory = false, def = DefaultValuePool.Null, description = "Line number  where it is needed to insert."
			+ "Numeration starts with 0. By default it will be inserted at end of the table.")
	protected Integer	index;
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		for (String column : columns)
		{
			if(column.isEmpty()) {
				super.setError("Column name can't be empty string", ErrorKind.EMPTY_PARAMETER);
				return;
			}
		}

		if (this.index != null)
		{
		    if (this.index < 0 || this.index > this.table.getHeaderSize())
		    {
		        super.setError("Index is out of bounds", ErrorKind.WRONG_PARAMETERS);
		        return;
		    }
			this.table.addColumns(this.index, this.columns);
		}
		else
		{
			this.table.addColumns(this.columns);
		}

		super.setResult(null);
	}
}


