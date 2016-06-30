package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "Rename selected columns into table",
		additionFieldsAllowed 	= true
)
public class TableColumnRename extends AbstractAction
{

	public static final String tableName = "Table";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "The table.")
	protected Table table = null;

	@Override
	public void initDefaultValues()
	{

	}

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters select = parameters.select(TypeMandatory.Extra);
		for (Parameter p : select)
		{
			this.table.renameColumn(p.getName(), p.getValue().toString());
		}
		super.setResult(null);
	}
}
