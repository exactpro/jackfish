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

package com.exactprosystems.jf.actions.system;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.app.ImageWrapper;
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.MatrixConnectionImpl;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.RowTable;
import com.exactprosystems.jf.functions.Table;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.Map.Entry;
import java.util.function.Function;

@ActionAttribute(
		group 					   = ActionGroups.System,
		suffix 					   = "RESTBL",
		constantGeneralDescription = R.RESULT_TABLE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.RESULT_TABLE_OUTPUT_DESC,
		outputType 				   = Table.class,
		constantExamples 		   = R.RESULT_TABLE_EXAMPLE,
		seeAlsoClass = {ResultTableUserValue.class}
)
public class ResultTable extends AbstractAction
{
	public static final String decoratedName = "Decorated";
	public static final String decoradedName = "Decoraded";
	public static final String matrixName    = "Matrix";


	@ActionFieldAttribute(name = decoratedName, mandatory = false, constantDescription = R.RESULT_TABLE_DECORATED)
	protected Boolean decorated;

	@ActionFieldAttribute(name = decoradedName, mandatory = false, constantDescription = R.RESULT_TABLE_DECORATED, deprecated = true)
	protected Boolean decoraded;

	@ActionFieldAttribute(name = matrixName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.RESULT_TABLE_MATRIX)
	protected MatrixConnectionImpl matrix;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (decoratedName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (decoratedName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);

		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Map<String, Object> map = new HashMap<>();
		if (this.matrix == null)
		{
			map.put(Context.resultColumn, Result.Passed);
		}

		Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if(this.decorated == null && this.decoraded == null)
		{
			super.setError("Please fill parameter \"Decorated\"", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		if(this.decorated != null && this.decoraded != null)
		{
			super.setError("Both params (\"Decorated\" and \"Decoraded\") are filled. Please remove one of them.", ErrorKind.EXCEPTION);
			return;
		}

		Table copy = new Table(result);
		if (report.reportIsOn())
		{        //TODO zzz
			copy.setValue(copy.size() - 1, map);
		}

		if ((this.decorated != null && this.decorated) || (this.decoraded != null && this.decoraded))
		{
			String passed = report.decorateStyle(Result.Passed.name(), Result.Passed.getStyle());
			Set<String> knownColumns = new HashSet<>(Arrays.asList(Context.resultColumns));

			for (RowTable row : copy)
			{
				Object matrix = row.get(Context.matrixColumn);
				if (matrix != null)
				{
					Path path = Paths.get(matrix.toString());
					Path pathName = path.getFileName();
					if (pathName != null)
					{
						String shortName = pathName.toString();
						String matrixStr = report.decorateExpandingBlock(shortName, matrix.toString());
						this.replace(row, Context.matrixColumn, e -> matrixStr);
					}
				}

				Result res = (Result) row.get(Context.resultColumn);
				String str = report.decorateStyle(row.get(Context.resultColumn), res == null ? "" : res.getStyle());
				row.put(Context.resultColumn, str);

				String stepIdentity = Str.asString(row.get(Context.stepIdentityColumn));
				row.put(Context.stepIdentityColumn, stepIdentity);
				row.put(Context.stepColumn, stepIdentity);

				this.replace(row, Context.testCaseColumn, Str::asString);
				this.replace(row, Context.testCaseIdColumn, Str::asString);
				this.replace(row, Context.timeColumn, e -> report.decorateStyle(e == null ? "" : (e + " ms"), "ExecutionTime"));

				Object error = row.get(Context.errorColumn);
				if (error instanceof MatrixError)
				{
					MatrixError matrixError = (MatrixError) error;
					String errorStr = report.decorateExpandingBlock(matrixError.Kind.toString(), matrixError.Message);
					replace(row, Context.errorColumn, e -> errorStr);
				}
				else
				{
					replace(row, Context.errorColumn, e -> passed);
				}

				Object wrapper = row.get(Context.screenshotColumn);
				if (wrapper instanceof ImageWrapper)
				{
					ImageWrapper iw = (ImageWrapper) wrapper;

					String description = iw.getDescription() == null ? iw.toString() : iw.getDescription();
					String imageStr = report.decorateLink(description, report.getImageDir() + File.separator + iw.getName(report.getReportDir()));
					replace(row, Context.screenshotColumn, e -> imageStr);
				}
				else
				{
					replace(row, Context.screenshotColumn, Str::asString);
				}

				for (Entry<String, Object> entry : row.entrySet())
				{
					String key = entry.getKey();
					if (knownColumns.contains(key))
					{
						continue;
					}
					Object value = row.get(key);
					row.put(key, value == null ? "" : value);
				}
			}
		}

		super.setResult(copy);
	}

	private void replace(RowTable row, String columnName, Function<Object, String> func)
	{
		Object value = row.get(columnName);
		row.put(columnName, func.apply(value));
	}
}


