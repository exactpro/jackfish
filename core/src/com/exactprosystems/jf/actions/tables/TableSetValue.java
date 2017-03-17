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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					= ActionGroups.Tables,
		generalDescription 		= "This action is used to set values in the cells of a table given.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "Set values for all cells in the line given. Each parameter name sets the column"
				+ " title, where the value is set. The value if a corresponding parameter sets the value of the column.",
		seeAlso = "{{@TableReplace@}}, {{@TableAddColumns@}}, {{@TableConsiderColumnAs@}}, {{@TableColumnRename@}}",
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class},
		examples = "{{`1. Create a table with columns Name and Age. Add values to the first line of the table.`}}"
				+ "{{`2. Set the value for the columns Name and Age 'John' and '42' in the first line.`}}"
				+ "{{`3. Verify that the new values were inserted correctly. `}}"
				+ "{{##Id;#RawTable\n"
				+ "TC;Table\n"
				+ "@;Name;Age\n"
				+ "0;Mike;25\n"
				+ "#EndRawTable\n"
				+ "#Action;#Table;#Index;#Name;#Age\n"
				+ "TableSetValue;TC;0;'John';'42'\n"
				+ "#Assert;#Message\n"
				+ "TC.get(0).get('Name') == 'John'&& TC.get(0).get('Age') == '42';#}}"
	)
public class TableSetValue extends AbstractAction 
{
	public final static String tableName = "Table";
	public final static String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, description = "Table which is needed to be changed")
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = true, description = "line number where it is needed to set the"
			+ " value. Numeration starts with 0.")
	protected Integer	index 	= 0;

	public TableSetValue()
	{
	}
	
    @Override
    public void initDefaultValues() 
    {
    }

    @Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
        Parameters params = parameters.select(TypeMandatory.Extra);
        for (String name : params.keySet())
        {
            if (!this.table.columnIsPresent(name))
            {
                super.setError("The header " + name + " does not exist in the table", ErrorKind.WRONG_PARAMETERS);
                return;
            }
        }
        
		this.table.setValue(this.index, params);
		super.setResult(null);
	}
}

