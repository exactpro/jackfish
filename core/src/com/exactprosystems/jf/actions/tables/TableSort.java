package com.exactprosystems.jf.actions.tables;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 				  = ActionGroups.Tables,
		suffix 				  = "TBLS",
		generalDescription 	  = "This action is used to sorting the table by the column given.",
		additionFieldsAllowed = true,
		outputDescription 	  = "Restores the original but sorted table.",
		outputType 			  = Table.class,
		examples = "{{` 1. Create a table with columns Name and Age. Complete the table with 3 lines.`}}"
				+ "{{`2. Do top-down sorting in  Age.`}}"
				+ "{{`3. Output the sorted table to report. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;42\n"
				+ "1;John;57\n"
				+ "2;Anna;21\n"
				+ "#EndRawTable\n"
				+ "#Action;#Ascending;#ColumnName;#Table\n"
				+ "TableSort;false;'Age';TC\n"
				+ "#Action;#Table;#Title\n"
				+ "TableReport;TC;'Table title'#}}"
)

public class TableSort extends AbstractAction
{
	public static final String tableName = "Table";
	public static final String columnName = "ColumnName";
	public static final String ascendingName = "Ascending";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Sorted table.")
	protected Table table = null;

	@ActionFieldAttribute(name = columnName, mandatory = true, description = "Column title that defines the table sorting.")
	protected String columnIndex = null;

	@ActionFieldAttribute(name = ascendingName, mandatory = false, def = DefaultValuePool.True, description = "Sorting type: true – ascending sorting,"
			+ " false – descending sorting. By default is true.")
	protected Boolean ascending;

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
		if(this.ascending == null)
		{
			super.setError("Column 'Ascending' can't be empty string", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if(!this.table.columnIsPresent(columnIndex))
		{
			super.setError("Column '" + columnIndex + "' doesn't exist in this table", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		super.setResult(this.table.sort(columnIndex, ascending));
	}
}
