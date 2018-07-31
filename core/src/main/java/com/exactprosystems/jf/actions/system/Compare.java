/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.common.report.ReportTable;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

@ActionAttribute(
        group                         = ActionGroups.System,
        constantGeneralDescription    = R.COMPARE_GENERAL_DESC,
        additionFieldsAllowed         = false,
        suffix                        = "CMP",
        outputType                    = Table.class,
        constantOutputDescription     = R.COMPARE_OUTPUT_DESC,
        constantAdditionalDescription = R.COMPARE_ADDITIONAL_DESC,
        constantExamples              = R.COMPARE_EXAMPLE
    )
public class Compare extends AbstractAction
{
	public static final String dontFailName = "DoNotFail";
	public static final String actualName   = "Actual";
	public static final String expectedName = "Expected";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.COMPARE_ACTUAL)
	protected Map<String, Object> actual;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.COMPARE_EXPECTED)
	protected Map<String, Object> expected;

	@ActionFieldAttribute(name = dontFailName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.COMPARE_DONT_FAIL)
	protected Boolean dontFail;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName)
	{
		if (dontFailName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (dontFailName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		String[] headers = new String[]{"Field", "Expected", "Actual", "Result"};
		ReportTable table = report.addTable("Comparing fields:", null, true, true, new int[]{25, 25, 25, 25}, headers);
		boolean res = true;
		Table resultTable = new Table(headers, evaluator);

		for (Entry<String, Object> expectedEntry : this.expected.entrySet())
		{
			String name = expectedEntry.getKey();
			Object expectedValue = expectedEntry.getValue();

			boolean found = this.actual.containsKey(name);
			Object actualValue = this.actual.get(name);
			Object[] line = null;

			if (found)
			{
				boolean comp = Objects.equals(expectedValue, actualValue);
				line = new Object[]{name, Str.asString(expectedValue), Str.asString(actualValue), comp ? Result.Passed : Result.Failed};
				res = res && comp;
			}
			else
			{
				line = new Object[]{name, Str.asString(expectedValue), "<not found>", Result.Failed};
				res = false;
			}

			table.addValues(line);
			resultTable.addValue(line);
		}

		super.setResult(resultTable);

		if (!res && !this.dontFail)
		{
			super.setError("Object does not match.", ErrorKind.NOT_EQUAL);
		}
	}

}
