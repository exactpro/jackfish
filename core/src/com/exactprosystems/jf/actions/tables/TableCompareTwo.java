////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
