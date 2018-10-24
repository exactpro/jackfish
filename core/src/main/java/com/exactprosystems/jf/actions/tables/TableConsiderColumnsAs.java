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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Table;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		constantGeneralDescription = R.TABLE_CONSIDER_COLUMN_AS_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples		   = R.TABLE_CONSIDER_COLUMN_AS_EXAMPLE
,
		seeAlsoClass = {TableReplace.class, TableAddColumns.class, TableColumnRename.class, TableAddValue.class}

	)
public class TableConsiderColumnsAs extends AbstractAction
{
	public static final String tableName        = "Table";
	public static final String asStringName     = "String";
	public static final String asBooleanName    = "Boolean";
	public static final String asIntegerName    = "Integer";
	public static final String asDoubleName     = "Double";
	public static final String asBigDecimalName = "BigDecimal";
	public static final String asDateName       = "Date";
	public static final String asExpressionName = "Expression";
	public static final String asGroupName      = "Group";
	public static final String asHyperlinkName  = "Hyperlink";
	public static final String asColoredName    = "Colored";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = asStringName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_STRING)
	protected String[] asString;

	@ActionFieldAttribute(name = asBooleanName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_BOOLEAN)
	protected String[] asBoolean;

	@ActionFieldAttribute(name = asIntegerName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_INTEGER)
	protected String[] asInteger;

	@ActionFieldAttribute(name = asDoubleName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_DOUBLE)
	protected String[] asDouble;

	@ActionFieldAttribute(name = asBigDecimalName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_BIG_DECIMAL)
	protected String[] asBigDecimal;

	@ActionFieldAttribute(name = asDateName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_DATE)
	protected String[] asDate;

	@ActionFieldAttribute(name = asExpressionName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_EXPRESSION)
	protected String[] asExpression;

	@ActionFieldAttribute(name = asGroupName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_GROUP)
	protected String[] asGroup;

	@ActionFieldAttribute(name = asHyperlinkName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_HYPERLINK)
	protected String[] asHyperlink;

	@ActionFieldAttribute(name = asColoredName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_CONSIDER_COLUMN_AS_COLORED)
	protected String[] asColored;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.asString.length > 0)
		{
			this.table.considerAsString(this.asString);
		}
		if (this.asBoolean.length > 0)
		{
			this.table.considerAsBoolean(this.asBoolean);
		}
		if (this.asInteger.length > 0)
		{
			this.table.considerAsInt(this.asInteger);
		}
		if (this.asDouble.length > 0)
		{
			this.table.considerAsDouble(this.asDouble);
		}
		if (this.asBigDecimal.length > 0)
		{
			this.table.considerAsBigDecimal(this.asBigDecimal);
		}
		if (this.asDate.length > 0)
		{
			this.table.considerAsDate(this.asDate);
		}
		if (this.asExpression.length > 0)
		{
			this.table.considerAsExpression(this.asExpression);
		}
		if (this.asGroup.length > 0)
		{
			this.table.considerAsGroup(this.asGroup);
		}
		if (this.asHyperlink.length > 0)
		{
			this.table.considerAsHyperlink(this.asHyperlink);
		}
		if (this.asColored.length > 0)
		{
			this.table.considerAsColored(this.asColored);
		}

		super.setResult(null);
	}
}

