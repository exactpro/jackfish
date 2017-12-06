////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.tables;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group 					   = ActionGroups.Tables,
		suffix                     = "TBLRR",
		constantGeneralDescription = R.TABLE_REMOVE_ROW_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TABLE_REMOVE_ROW_OUTPUT_DESC,
		outputType 				   = Boolean.class,
		constantExamples 		   = R.TABLE_REMOVE_ROW_EXAMPLE
,
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class}
)
public class TableRemoveRow extends AbstractAction
{
	public static final String tableName = "Table";
	public static final String rowIndex  = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_REMOVE_ROW_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = rowIndex, mandatory = true, constantDescription = R.TABLE_REMOVE_ROW_ROW)
	protected Integer row;

	@Override
	protected void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.row >= this.table.size() || this.row < 0)
		{
			super.setError("The index is out of bound of the table", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		super.setResult(this.table.removeRow(this.row));
	}
}
