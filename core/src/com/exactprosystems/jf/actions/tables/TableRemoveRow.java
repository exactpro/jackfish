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
		group = ActionGroups.Tables,
		suffix = "TBLRR",
		generalDescription 		= "This action is used to delete a selected line in a table given.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if removing is successful",
		outputType 				= Boolean.class,
		examples 				=
				"{{` 1. Create a table with columns Name and Age. Add two lines with data to the table. `}}"
				+ "{{` 2. Delete the first line in the table, the line with the index 0. `}}"
				+ "{{` 3. Verify that the first line was deleted and was replaced with the line containing data about Anna. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;25\n"
				+ "1;Anna;20\n"
				+ "#EndRawTable\n"
				+ "#Id;#Action;#Table;#Index\n"
				+ "TBLRR1;TableRemoveRow;TC;0\n"
				+ "#Assert;#Message\n"
				+ "TC.get(0).get('Name') == 'Anna';'Table is not correct'#}}",
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class}
)
public class TableRemoveRow extends AbstractAction
{
	public final static String tableName = "Table";
	public static final String rowIndex = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table where a line needs to be deleted.")
	protected Table table = null;

	@ActionFieldAttribute(name = rowIndex, mandatory = true, description = "A deletable line number. In case of a"
			+ " negative value the last line will be deleted. Numeration starts with 0.")
	protected Integer row = null;

	public TableRemoveRow()
	{
	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(table.removeRow(row));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
