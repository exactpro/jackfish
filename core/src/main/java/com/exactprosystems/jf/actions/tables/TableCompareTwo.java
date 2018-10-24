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
import com.exactprosystems.jf.functions.Table.TableCompareResult;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@ActionAttribute(
		group					   = ActionGroups.Tables,
		suffix					   = "TBLCMP",
		constantGeneralDescription = R.TABLE_COMPARE_TWO_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		outputType                 = Table.class,
		constantOutputDescription  = R.TABLE_COMPARE_TWO_OUTPUT_DESC,
		constantExamples 		   = R.TABLE_COMPARE_TWO_EXAMPLE
	)
public class TableCompareTwo extends AbstractAction
{
	public static final String actualName          = "Actual";
	public static final String expectedName        = "Expected";
	public static final String excludeName         = "ExcludeColumns";
	public static final String ignoreRowsOrderName = "IgnoreRowsOrder";
	public static final String compareValuesName   = "CompareValues";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.TABLE_COMPARE_TWO_ACTUAL)
	protected Table actual;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.TABLE_COMPARE_TWO_EXPECTED)
	protected Table expected;

	@ActionFieldAttribute(name = excludeName, mandatory = false, def = DefaultValuePool.EmptyArrString, constantDescription = R.TABLE_COMPARE_TWO_EXCLUDE)
	protected String[] exclude;

	@ActionFieldAttribute(name = ignoreRowsOrderName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_COMPARE_TWO_IGNORE_ROWS_ORDER)
	protected Boolean ignoreRowsOrder;

	@ActionFieldAttribute(name = compareValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_COMPARE_TWO_COMPARE_VALUES)
	protected Boolean compareValues;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return ignoreRowsOrderName.equals(fieldName) || compareValuesName.equals(fieldName) ? HelpKind.ChooseFromList : null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case ignoreRowsOrderName:
			case compareValuesName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Set<String> actualColumns = this.actual.names(this.exclude);
		Set<String> expectedColumns = this.expected.names(this.exclude);

		if (!Objects.equals(actualColumns, expectedColumns))
		{
			super.setError("Actual columns " + actualColumns + " doesn't match expected columns " + expectedColumns, ErrorKind.WRONG_PARAMETERS);
			return;
		}

		Table differences = new Table(new String[]{"Description", "Expected", "Actual"}, evaluator);
		TableCompareResult res = Table.extendEquals(report, differences, this.actual, this.expected, this.exclude, this.ignoreRowsOrder, this.compareValues);

		super.setResult(differences);
		if (!res.equal)
		{
			String message = String.format("Tables are not equal.%n %d - matched%n %d - extra actual%n %d - extra expected", res.matched, res.extraActual, res.extraExpected);

			super.setError(message, ErrorKind.NOT_EQUAL);
		}
	}
}
