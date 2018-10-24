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
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		suffix					   = "TBL",
		constantGeneralDescription = R.TABLE_COLUMN_AS_LIST_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TABLE_COLUMN_AS_LIST_OUTPUT_DESC,
		outputType				   = List.class,
        constantExamples           = R.TABLE_COLUMN_AS_LIST_EXAMPLE,
		seeAlsoClass = { Table.class }
	)
public class TableColumnAsList extends AbstractAction
{
	public static final String tableName     = "Table";
	public static final String columnName    = "Column";
	public static final String getValuesName = "GetValues";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_COLUMN_AS_LIST_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = columnName, mandatory = true, constantDescription = R.TABLE_COLUMN_AS_LIST_COLUMN)
	protected String column;

	@ActionFieldAttribute(name = getValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_COLUMN_AS_LIST_GET_VALUES)
	protected Boolean getValues;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (getValuesName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (getValuesName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (!this.table.columnIsPresent(this.column))
		{
			setError(String.format("Columns with name '%s' is not presented in table", this.column), ErrorKind.WRONG_PARAMETERS);
			return;
		}
		super.setResult(this.table.getColumnAsList(this.column, this.getValues));
	}
}
