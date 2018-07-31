/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.*;
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
	public static final String tableName = "Table";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_ADD_VALUE_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = indexName, mandatory = false, def = DefaultValuePool.IntMin, constantDescription = R.TABLE_ADD_VALUE_INDEX)
	protected Integer index;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters params = parameters.select(TypeMandatory.Extra);
		for (String name : params.keySet())
		{
			if (!this.table.columnIsPresent(name))
			{
				super.setError(String.format("The header %s does not exist in the table", name), ErrorKind.WRONG_PARAMETERS);
				return;
			}
		}
		this.table.addValue(this.index, params.makeCopy());
		super.setResult(null);
	}
}

