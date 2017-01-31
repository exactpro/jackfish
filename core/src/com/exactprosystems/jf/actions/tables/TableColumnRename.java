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
		generalDescription 		= "This action is determined to rename a column in the table given.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "The column name given is indicated in a parameter’s name, a new column name is in a parameter’s value.",
		seeAlso = "{{@TableReplace@}}, {{@TableAddColumns@}}, {{@TableConsiderColumnAs@}}, {{@TableAddValue@}}",
		examples 				=
				"{{`1. Create a table with columns Name and Age.`}}"
				+ "{{`2. Rename column Name into FirstName.`}}"
				+ "{{`3. Verify that a new name is assigned to a column. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;;\n"
				+ "#EndRawTable\n"
				+ "\n"
				+ "\n"
				+ "#Action;#Table;#Name\n"
				+ "TableColumnRename;TC;'FirstName'\n"
				+ "\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "TC.getHeader(0) == 'FirstName';'Values is not equals'#}}"
)
public class TableColumnRename extends AbstractAction
{

	public static final String tableName = "A table where a column / columns are needed to rename. ";

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
