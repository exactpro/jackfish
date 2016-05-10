package com.exactprosystems.jf.actions.tables;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group = ActionGroups.Tables,
		suffix = "TBLS",
		generalDescription = "Sorting presented table",
		additionFieldsAllowed = true,
		outputDescription = "Sorted table",
		outputType = Table.class)

public class TableSort extends AbstractAction
{
	public static final String tableName = "Table";
	public static final String columnName = "ColumnName";
	public static final String ascendingName = "Ascending"; // po vozrastaniu

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table table = null;

	@ActionFieldAttribute(name = columnName, mandatory = true, description = "Column sorting name")
	protected String columnIndex = null;

	@ActionFieldAttribute(name = ascendingName, mandatory = false, description = "Sorting policy (true, if ascending, and false, if descending")
	protected Boolean ascending;

	public TableSort()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		ascending = true;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case ascendingName:
			case columnName:
				return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ascendingName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
				
			case columnName:
				Object tab = parameters.get(tableName);
				if (tab instanceof Table)
				{
					Table table = (Table)tab;
					for (int index = 0; index < table.getHeaderSize(); index++)
					{
						list.add(new ReadableValue(context.getEvaluator().createString(table.getHeader(index))));
					}
				}
				break;
		}
	}
	
	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.table.sort(columnIndex, ascending));
	}
}
