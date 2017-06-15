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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action  is determined to add lines to a Table..",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "Are used to insert values where a parameter name - a column name, a parameter value - a cell value.",
		examples 				=
				"{{`1. Create a table with columns Name and Age. Add a value into 0 line to a column Name. A cell in a column Age leave empty.`}}"
				+ "{{`2. Verify that a value in the first line of a column Name is equal 'Mike', Age is equal  null. Column Age is filled with a value null. `}}"
				+ "{{#\n#Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;;\n"
				+ "#EndRawTable\n"
				+ "#Action;#Index;#Table;#Name\n"
				+ "TableAddValue;0;TC;'Mike'\n"
				+ "#Assert;#Message\n"
				+ "TC.get(0).get('Name') == 'Mike' && TC.get(0).get('Age') == null;'Values are not equals'#}}",
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class}
	)
public class TableAddValue extends AbstractAction 
{
	public final static String tableName = "Table";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "A table where it is needed to add a line.")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = false, def = DefaultValuePool.IntMin, description = "A line index, where a new line is added. "
			+ "If it is a negative value it will be inserted at the end of the table. Numeration starts with 0.")
	protected Integer	index;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{

		if (this.index == null)
		{
			super.setError("Index is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

        Parameters params = parameters.select(TypeMandatory.Extra);
        for (String name : params.keySet())
        {
            if (!this.table.columnIsPresent(name))
            {
                super.setError("The header " + name + " does not exist in the table", ErrorKind.WRONG_PARAMETERS);
                return;
            }
        }
	    
		table.addValue(index, params);
		super.setResult(null);
	}
}

