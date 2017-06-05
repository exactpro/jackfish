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
				"{{#" +
						"#Id;#RawTable\n" +
						"main;Table\n" +
						"@;Name;Age;Gender\n" +
						"0;Mike;42;Male\n" +
						"1;Anna;21;Female\n" +
						"#EndRawTable\n" +
						"\n" +
						"#Id;#RawTable\n" +
						"additional;Table\n" +
						"@;Name;Age\n" +
						"0;Fred;28\n" +
						"1;Carl;10\n" +
						"#EndRawTable\n" +
						"\n" +
						"#Action;$UnitedTable;$MainTable\n" +
						"TableUnion;additional;main\n" +
						"\n" +
						"#Action;$Table;$Title\n" +
						"TableReport;main;'dfdf'\n" +
						"\n" +
						"#Id;#RawTable\n" +
						"expected;Table\n" +
						"@;Name;Age;Gender\n" +
						"0;Mike;42;Male\n" +
						"1;Anna;21;Female\n" +
						"2;Fred;28;\n" +
						"3;Carl;10;\n" +
						"#EndRawTable\n" +
						"\n" +
						"#Id;#Action;$Expected;$Actual\n" +
						"TBLCMP1;TableCompareTwo;expected;main\n" +
						"\n" +
						"#}}"
)
public class TableUnion extends AbstractAction
{
	public final static String mainTableName = "MainTable";
	public final static String unitedTableName = "UnitedTable";

	@ActionFieldAttribute(name = mainTableName, mandatory = true, description = "Table where lines will be added.")
	protected Table mainTable = null;

	@ActionFieldAttribute(name = unitedTableName, mandatory = true, description = "Table with the lines that will be added to the main one")
	protected Table unitedTable = null;

	@Override
	public void doRealAction(Context context, ReportBuilder report,	Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		boolean atLeastOneCoincidence = false;
		for (int mainNum = 0; mainNum < this.mainTable.getHeaderSize(); mainNum++)
		{
			for (int unitedNum = 0; unitedNum < this.unitedTable.getHeaderSize(); unitedNum++)
			{
				if(this.mainTable.getHeader(mainNum).equals(this.unitedTable.getHeader(unitedNum)))
				{
					atLeastOneCoincidence = true;
					break;
				}
			}
		}

		if(atLeastOneCoincidence)
		{
			for (RowTable row : this.unitedTable)
			{
				this.mainTable.add(row);
			}
		}

		super.setResult(null);
	}
}
