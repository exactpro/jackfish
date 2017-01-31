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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					= ActionGroups.Tables, 
		suffix 					= "TBLJN", 
		generalDescription 		= "This action is determined to join tables as in SQL Left Join.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "Additional parameters are used to assign column titles.",
		outputDescription 		= "Table structure.",
		outputType				= Table.class,
		examples 				=
				"{{`1. Create a table with columns Name and Age, Gender. Populate it with two rows.`}}"
				+ "{{`2. Create a table with columns City, Street, Name. Populate it with two rows.`}}"
				+ "{{`3. Use Left Join choosing rows from the first table on condition: matching column Name. `}}"
				+ "{{##Id;#TestCase\n"
				+ "Test;\n"
				+ "    #Id;#RawTable\n"
				+ "    TC;Table\n"
				+ "    @;Name;Age;Gender\n"
				+ "    0;Mike;42;Male\n"
				+ "    1;Olga;21;Female\n"
				+ "    #EndRawTable\n"
				+ "\n"
				+ "    #Id;#RawTable\n"
				+ "    TC1;Table\n"
				+ "    @;City;Street;Name\n"
				+ "    0;London;12;Mike\n"
				+ "    1;London;3;Anna\n"
				+ "    #EndRawTable\n"
				+ "\n"
				+ "    #Id;#Action;#Condition;#LeftTable;#RightAlias;#LeftAlias;#RightTable\n"
				+ "    TBLJN1;TableLeftJoin;'a.Name == b.Name';TC;'b';'a';TC1\n"
				+ "\n"
				+ "    #Action;#Table;#Title\n"
				+ "    TableReport;TBLJN1.Out;'title'#}}"
)
public class TableLeftJoin extends AbstractAction
{
	public final static String rightTableName 	= "RightTable";
	public final static String leftTableName 	= "LeftTable";
	public final static String rightAliasName 	= "RightAlias";
	public final static String leftAliasName 	= "LeftAlias";
	public final static String conditionName 	= "Condition";

	@ActionFieldAttribute(name = rightTableName, mandatory = true, description = "Table, which rows are matched according to condition.")
	protected Table rightTable = null;

	@ActionFieldAttribute(name = leftTableName, mandatory = true, description = "Table, which rows are selected according to condition.")
	protected Table leftTable = null;

	@ActionFieldAttribute(name = rightAliasName, mandatory = true, description = "Alias for the right table rows.")
	protected String rightAlias = null;

	@ActionFieldAttribute(name = leftAliasName, mandatory = true, description = "Alias for the left table rows.")
	protected String leftAlias = null;

	@ActionFieldAttribute(name = conditionName, mandatory = true, description = "Condition under which row sample is made.")
	protected String condition = null;

	public TableLeftJoin()
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
		Table newTable = this.leftTable.clone();
		newTable.clear(); // (fi!)
		
		for (Parameter column : extra)
		{
			newTable.addColumns(column.getName());
		}
		
		for (RowTable rowLeft : this.leftTable)
		{
			evaluator.getLocals().set(this.leftAlias, rowLeft);
			
			for (RowTable rowRight : this.rightTable)
			{
				evaluator.getLocals().set(this.rightAlias, rowRight);
				
				Object cond = evaluator.evaluate(this.condition);
				if (cond instanceof Boolean)
				{
					boolean on = ((Boolean)cond).booleanValue();
					if (on)
					{
						RowTable newRow = rowLeft.clone();
						for (Parameter column : extra)
						{
							newRow.put(column.getName(), evaluator.evaluate("" + column.getValue()));
						}
						newTable.add(newRow);
					}
				}
				else 
				{
					super.setError("Join condition must be Boolean", ErrorKind.WRONG_PARAMETERS);
					return;
				}
			}
		}
		
		super.setResult(newTable);
	}
}
