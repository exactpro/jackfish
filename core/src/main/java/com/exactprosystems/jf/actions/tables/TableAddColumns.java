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

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					  	= ActionGroups.Tables,
		constantGeneralDescription 	= R.TABLE_ADD_COLUMNS_GENERAL_DESC,
		additionFieldsAllowed 	  	= false,
		constantExamples   			= R.TABLE_ADD_COLUMNS_EXAMPLES,
		seeAlsoClass 			    = {TableReplace.class, TableColumnRename.class, TableAddValue.class, TableConsiderColumnsAs.class}
	)
public class TableAddColumns extends AbstractAction 
{
	public static final String tableName   = "Table";
	public static final String columnsName = "Columns";
	public static final String indexName   = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_ADD_COLUMNS_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = columnsName, mandatory = true, constantDescription = R.TABLE_ADD_COLUMNS_COLUMNS)
	protected String[] columns;

	@ActionFieldAttribute(name = indexName, mandatory = false, constantDescription = R.TABLE_ADD_COLUMNS_INDEX)
	protected Integer index;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		for (String column : this.columns)
		{
			if (column.isEmpty())
			{
				super.setError("Column name can't be empty", ErrorKind.EMPTY_PARAMETER);
				return;
			}
		}

		if (this.index != null)
		{
			if (this.index < 0 || this.index > this.table.getHeaderSize())
			{
				super.setError("Index is out of bounds", ErrorKind.WRONG_PARAMETERS);
				return;
			}
			this.table.addColumns(this.index, this.columns);
		}
		else
		{
			this.table.addColumns(this.columns);
		}

		super.setResult(null);
	}
}


