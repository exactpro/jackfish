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
		generalDescription = "Remove row by index from table",
		additionFieldsAllowed = false,
		outputDescription = "True if removing is successful",
		outputType = Boolean.class)
public class TableRemoveRow extends AbstractAction
{
	public final static String tableName = "Table";
	public static final String rowIndex = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table table = null;

	@ActionFieldAttribute(name = rowIndex, mandatory = true, description = "Row index")
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
