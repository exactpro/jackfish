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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.RawTable;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		suffix					= "TBLSLCT",
		generalDescription 		= "This action is used for extracting lines from the table given and creating a new"
				+ " table with these lines. Can be applied when it Is needed to get only a part of data from the table."
				+ "Line extracting is done according to the content which is specified in additional parameters.",
		additionFieldsAllowed 	= true,
		additionalDescription   = "Columns containing the data which defines the selection. Column title is given in"
				+ " the parameter’s value. In the value it is needed to specify the content which defines the selection.",
		outputDescription 		= "Outputs the table containing the selected lines.",
		outputType				= Table.class,
		seeAlso 				= "{{@RawTable@}}, {{@TableLoadFromDir@}}, {{@TableLoadFromFile@}}, {{@TableCreate@}}",
		seeAlsoClass = {RawTable.class, TableLoadFromDir.class, TableLoadFromFile.class, TableCreate.class},
		examples = "{{`Example #1:`}}"
				+ "{{`1. Create a table with columns Name and Age. Complete the table with 3 lines.`}}"
				+ "{{`2. Set the data type  Integer for the column Age.`}}"
				+ "{{`3. With the actionTableSelect select all lines with the value 'Mike' in the column Name.`}}"
				+ "{{`4. Verify that the size of the table is equal to 1. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;42\n"
				+ "1;John;42\n"
				+ "2;Fred;21\n"
				+ "#EndRawTable\n"
				+ "#Action;#Table;#Integer\n"
				+ "TableConsiderColumnsAs;TC;'Age'\n"
				+ "#Id;#Action;#Table;#Age\n"
				+ "TS;TableSelect;TC;new StringCondition('Name','Mike')\n"
				+ "#Assert;#Message\n"
				+ "TS.Out.size() == 1;#}}\n"
				+ "{{`Example #2:`}}"
				+ "{{`1. Create a table with columns Name and Age. Complete the table with 3 lines.`}}"
				+ "{{`2. Set the data type  Integer for the column Age.`}}"
				+ "{{`3. With the action TableSelect selectall lines where the column values Age are more than 20 and less than 40`}}"
				+ "{{`4. Verify that the size of the table is equal to 2. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;42\n"
				+ "1;John;34\n"
				+ "2;Fred;21\n"
				+ "#EndRawTable\n"
				+ "#Action;#Table;#Integer\n"
				+ "TableConsiderColumnsAs;TC;'Age'\n"
				+ "#Id;#Action;#Table;#Age\n"
				+ "TS;TableSelect;TC;new AndCondition(new NumberCondition('Age','>',20), new NumberCondition('Age','<',40))\n"
				+ "#Assert;#Message\n"
				+ "TS.Out.size() == 2;#}}"
	)
public class TableSelect extends AbstractAction 
{
	public final static String tableName = "Table";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table from which rows are selected")
	protected Table 	table 	= null;

	public TableSelect()
	{
	}
	
	@Override
	public void initDefaultValues() 
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

