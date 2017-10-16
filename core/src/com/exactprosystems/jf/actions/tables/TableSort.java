package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group 				       = ActionGroups.Tables,
		suffix 				       = "TBLS",
		constantGeneralDescription = R.TABLE_SORT_GENERAL_DESC,
		additionFieldsAllowed      = true,
		constantOutputDescription  = R.TABLE_SORT_OUTPUT_DESK,
		outputType 			       = Table.class,
		constantExamples 		   = R.TABLE_SORT_EXAMPLE
	)

public class TableSort extends AbstractAction
{
	public static final String tableName = "Table";
	public static final String columnName = "ColumnName";
	public static final String ascendingName = "Ascending";
	public static final String ignoreCaseName = "IgnoreCase";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Sorted table.")
	protected Table table = null;

	@ActionFieldAttribute(name = columnName, mandatory = true, description = "Column title that defines the table sorting.")
	protected String columnIndex = null;

	@ActionFieldAttribute(name = ascendingName, mandatory = false, def = DefaultValuePool.True, description = "Sorting type: true - ascending sorting,"
			+ " false - descending sorting. By default is true.")
	protected Boolean ascending;

	@ActionFieldAttribute(name = ignoreCaseName, mandatory = false, def = DefaultValuePool.False, description = "If this ignoreCase is true - sorting will ignore case. Available only for strings")
	protected Boolean ignoreCase;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case ascendingName:
			case ignoreCaseName:
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
			case ignoreCaseName:
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

		super.setResult(this.table.sort(this.columnIndex, this.ascending, this.ignoreCase));
	}
}
