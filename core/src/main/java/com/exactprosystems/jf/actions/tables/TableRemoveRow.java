/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
