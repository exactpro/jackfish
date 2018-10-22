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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Table;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@ActionAttribute(
		group					      = ActionGroups.Tables,
		constantGeneralDescription    = R.TABLE_REPORT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
        constantAdditionalDescription = R.TABLE_REPORT_ADDITIONAL_DESC,
		constantExamples 			  = R.TABLE_REPORT_EXAMPLE
	)
public class TableReport extends AbstractAction 
{
	public static  final String tableName          = "Table";
	public static  final String beforeTestCaseName = "BeforeTestCase";
	public static  final String titleName          = "Title";
	public static  final String numbersName        = "Numbers";
	public static  final String columnsName        = "Columns";
	public static  final String reportValuesName   = "ReportValues";
	public static  final String toReportName       = "ToReport";
	public static  final String widthName          = "Width";

	@ActionFieldAttribute(name = tableName, mandatory = true, constantDescription = R.TABLE_REPORT_TABLE)
	protected Table table;

	@ActionFieldAttribute(name = titleName, mandatory = true, constantDescription = R.TABLE_REPORT_TITLE)
	protected String title;

	@ActionFieldAttribute(name = toReportName, mandatory = false, constantDescription = R.TABLE_REPORT_TO_REPORT)
	protected ReportBuilder toReport;

	@ActionFieldAttribute(name = beforeTestCaseName, mandatory = false, constantDescription = R.TABLE_REPORT_BEFORE_TEST_CASE)
	protected String beforeTestCase;

	@ActionFieldAttribute(name = numbersName, mandatory = false, def = DefaultValuePool.True, constantDescription = R.TABLE_REPORT_WITH_NUMBERS)
	protected Boolean withNumbers;

	@ActionFieldAttribute(name = columnsName, mandatory = false, constantDescription = R.TABLE_REPORT_COLUMNS)
	protected Object columns;

	@ActionFieldAttribute(name = widthName, mandatory = false, constantDescription = R.TABLE_REPORT_WIDTHS)
	protected Integer[] widths;

	@ActionFieldAttribute(name = reportValuesName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.TABLE_REPORT_REPORT_VALUES)
	protected Boolean reportValues;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case beforeTestCaseName:
			case numbersName:
			case reportValuesName:
				return HelpKind.ChooseFromList;
		}

		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case numbersName:
			case reportValuesName:
				list.add(ReadableValue.TRUE);
				list.add(ReadableValue.FALSE);
				break;
			case beforeTestCaseName:
				ActionsReportHelper.fillListForParameter(super.owner.getMatrix(), list, context.getEvaluator());
				break;
			default:
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, String> map = new LinkedHashMap<>();
		if (this.columns == null)
		{
			Parameters extra = parameters.select(TypeMandatory.Extra);
			for (Parameter ex : extra)
			{
				map.put(ex.getName(), ex.getValueAsString());
			}
		}
		else
		{
			if (this.columns.getClass().isArray())
			{
				map = Arrays.stream((Object[]) this.columns)
						.map(Str::asString)
						.collect(Collectors.toMap(a -> a, a -> a, (k, v) -> k, LinkedHashMap::new));
			}
			else if (this.columns instanceof Iterable<?>)
			{
				map = StreamSupport.stream(((Iterable<?>) this.columns).spliterator(), false)
						.map(Str::asString)
						.collect(Collectors.toMap(a -> a, a -> a, (k, v) -> k, LinkedHashMap::new));
			}
			else if (this.columns instanceof Map<?, ?>)
			{
				map = ((Map<?, ?>) this.columns).entrySet()
						.stream()
						.collect(Collectors.toMap(e -> Str.asString(e.getKey()), e -> Str.asString(e.getValue()), (k, v) -> k, LinkedHashMap::new));
			}
			else
			{
				String key = Str.asString(this.columns);
				map.put(key, key);
			}
		}

		int[] width = null;
		if (this.widths != null)
		{
			width = Arrays.stream(this.widths).mapToInt(Integer::intValue).toArray();
		}

		report = this.toReport == null ? report : this.toReport;
		this.beforeTestCase = ActionsReportHelper.getBeforeTestCase(this.beforeTestCase, this.owner.getMatrix());
		this.table.report(report, Str.asString(this.title), this.beforeTestCase, this.withNumbers, this.reportValues, true, map, width);

		super.setResult(null);
	}
}

