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
		generalDescription 		= "This action is used to add all lines from one table  to the end of another one."
				+ " With the table merge, Main Table columns have priority, that means that merging with the table "
				+ "with more columns extra ones will be ignored. In case of adding the table with not enough columns"
				+ " from the main one, the values in such columns will be null.",
		additionFieldsAllowed 	= false,
		examples 				=
				"{{`1. Create a table with columns Name,  Age and Gender. Complete the table with 2 lines.`}}"
				+ "{{`2. Create a table with columns Name and Age. Complete the table with 2 lines.`}}"
				+ "{{`3. Add the lines from the second table to the end of the first one.`}}"
				+ "{{`4. Verify that the table size is equal to 4. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age;Gender\n"
				+ "0;Mike;42;Male\n"
				+ "1;Anna;21;Female\n"
				+ "#EndRawTable\n"
				+ "#Id;#RawTable\n"
				+ "TC1;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;42\n"
				+ "1;Anna;21\n"
				+ "#EndRawTable\n"
				+ "#Action;#UnitedTable;#MainTable\n"
				+ "TableUnion;TC1;TC\n"
				+ "#Assert;#Message\n"
				+ "TC.size() == 4;#}}"
)
public class TableUnion extends AbstractAction
{
	public final static String mainTableName = "MainTable";
	public final static String unitedTableName = "UnitedTable";

	@ActionFieldAttribute(name = mainTableName, mandatory = true, description = "Table where lines will be added.")
	protected Table mainTable = null;

	@ActionFieldAttribute(name = unitedTableName, mandatory = true, description = "Table with the lines that will be added to the main one")
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
