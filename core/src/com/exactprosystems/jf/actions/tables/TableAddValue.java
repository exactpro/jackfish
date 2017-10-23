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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		constantGeneralDescription    = R.TABLE_ADD_VALUE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.TABLE_ADD_VALUE_ADDITIONAL_DESC,
		constantExamples              = R.TABLE_ADD_VALUE_EXAMPLE,
		seeAlsoClass                  = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class}
	)
public class TableAddValue extends AbstractAction 
{
	public final static String tableName = "Table";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_ADD_VALUE_TABLE)
	protected Table 	table 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.TABLE_ADD_VALUE_INDEX)
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
	    
		table.addValue(index, params.makeCopy());
		super.setResult(null);
	}
}

