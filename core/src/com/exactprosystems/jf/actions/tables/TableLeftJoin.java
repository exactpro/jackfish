////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
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
		group 					      = ActionGroups.Tables,
		suffix 					      = "TBLJN",
		constantGeneralDescription    = R.TABLE_LEFT_JOIN_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.TABLE_LEFT_JOIN_ADDITIONAL_DESC,
		constantOutputDescription     = R.TABLE_LEFT_JOIN_OUTPUT_DESC,
		outputType				      = Table.class,
		constantExamples 		 	  = R.TABLE_LEFT_JOIN_EXAMPLE
)
public class TableLeftJoin extends AbstractAction
{
	public final static String rightTableName 	= "RightTable";
	public final static String leftTableName 	= "LeftTable";
	public final static String rightAliasName 	= "RightAlias";
	public final static String leftAliasName 	= "LeftAlias";
	public final static String conditionName 	= "Condition";

	@ActionFieldAttribute(name = rightTableName, mandatory = true, constantDescription = R.TABLE_LEFT_JOIN_RIGHT_TABLE)
	protected Table rightTable = null;

	@ActionFieldAttribute(name = leftTableName, mandatory = true, constantDescription = R.TABLE_LEFT_JOIN_LEFT_TABLE)
	protected Table leftTable = null;

	@ActionFieldAttribute(name = rightAliasName, mandatory = true, constantDescription = R.TABLE_LEFT_JOIN_RIGHT_ALIAS)
	protected String rightAlias = null;

	@ActionFieldAttribute(name = leftAliasName, mandatory = true, constantDescription = R.TABLE_LEFT_JOIN_LEFT_ALIAS)
	protected String leftAlias = null;

	@ActionFieldAttribute(name = conditionName, mandatory = true, constantDescription = R.TABLE_LEFT_JOIN_CONDITION)
	protected String condition = null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters extra = parameters.select(TypeMandatory.Extra);
		Table newTable = new Table(this.leftTable);
		newTable.clear();

		for (Parameter column : extra)
		{
			newTable.addColumns(column.getName());
		}

		if(this.leftAlias.isEmpty())
		{
			super.setError("Column '" + leftAliasName + "' can't be empty string.", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if(this.rightAlias.isEmpty())
		{
			super.setError("Column '" + rightAliasName + "' can't be empty string.", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if(evaluator.getLocals().getVariable(this.leftAlias) != null)
		{
			super.setError("Variable with name '" + this.leftAlias + "' already exist", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		if(evaluator.getLocals().getVariable(this.rightAlias) != null)
		{
			super.setError("Variable with name '" + this.rightAlias + "' already exist", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		for (RowTable rowLeft : this.leftTable)
		{
            boolean hasColumn = false;
			evaluator.getLocals().set(this.leftAlias, rowLeft);

			for (RowTable rowRight : this.rightTable)
			{
				evaluator.getLocals().set(this.rightAlias, rowRight);

				Object cond = evaluator.evaluate(this.condition);
				if (cond instanceof Boolean)
				{
				    if((Boolean) cond)
                    {
                        hasColumn = true;
                        RowTable newRow = newTable.addNew();
                        newRow.putAll(rowLeft);
                        for (Parameter column : extra)
                        {
                            newRow.put(column.getName(), evaluator.evaluate("" + column.getValue()));
                        }
                    }
				}
				else
				{
					super.setError("Join condition must be Boolean", ErrorKind.WRONG_PARAMETERS);
					return;
				}
			}

			if(!hasColumn)
			{
				RowTable newRow = newTable.addNew();
				newRow.putAll(rowLeft);
				for (Parameter column : extra)
				{
					newRow.put(column.getName(), "");
				}
			}
		}

		super.setResult(newTable);
	}
}
