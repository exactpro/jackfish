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
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		constantGeneralDescription    = R.TABLE_SET_VALUE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.TABLE_SET_VALUE_ADDITIONAL_DESC,
		seeAlsoClass       			  = {TableReplace.class, TableAddColumns.class, TableConsiderColumnsAs.class, TableColumnRename.class},
		constantExamples 			  = R.TABLE_SET_VALUE_EXAMPLE
	)
public class TableSetValue extends AbstractAction 
{
	public static final String tableName = "Table";
	public static final String indexName = "Index";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_SET_VALUE_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = indexName, mandatory = true, constantDescription = R.TABLE_SET_VALUE_INDEX)
	protected Integer index;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Parameters params = parameters.select(TypeMandatory.Extra);
		for (String name : params.keySet())
		{
			if (name.isEmpty())
			{
				super.setError("The column name does not have to contain an empty value", ErrorKind.EMPTY_PARAMETER);
				return;
			}

			if (!this.table.columnIsPresent(name))
			{
				super.setError(String.format("The header %s does not exist in the table", name), ErrorKind.WRONG_PARAMETERS);
				return;
			}
		}

		if (this.index >= this.table.size() || this.index < 0)
		{
			super.setError("The index is out of bound of the table", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		this.table.setValue(this.index, params.makeCopy());
		super.setResult(null);
	}
}

