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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.MatrixConnectionImpl;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Table;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@ActionAttribute(
		group 						  = ActionGroups.System,
		suffix 						  = "",
		constantGeneralDescription    = R.RESULT_TABLE_USER_VALUE_GENERAL_DESC,
		additionFieldsAllowed 		  = true,
		constantAdditionalDescription = R.RESULT_TABLE_USER_VALUE_ADDITIONAL_DESC,
		constantExamples 			  = R.RESULT_TABLE_USER_VALUE_EXAMPLE,
		seeAlsoClass 				  = {ResultTable.class}
)
public class ResultTableUserValue extends AbstractAction
{
	public static final String matrixName = "Matrix";

	@ActionFieldAttribute(name = matrixName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.RESULT_TABLE_USER_VALUE_MATRIX)
	protected MatrixConnectionImpl matrix;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Table result = this.matrix == null ? context.getTable() : this.matrix.getTable();
		if (result == null)
		{
			super.setError("The result table is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		int size = result.size();
		if (size == 0)
		{
			super.setError("The result table is empty", ErrorKind.FAIL);
			return;
		}
		Set<String> predefinedColumns = new HashSet<>(Arrays.asList(Context.resultColumns));
		Parameters extraParams = parameters.select(TypeMandatory.Extra);

		for (String name : extraParams.keySet())
		{
			if (predefinedColumns.contains(name))
			{
				super.setError("Parameter '" + name + "' is reserved word.", ErrorKind.WRONG_PARAMETERS);
				return;
			}
		}

		for (Parameter parameter : extraParams)
		{
			String name = parameter.getName();
			Object value = parameter.getValue();
			if (predefinedColumns.contains(name))
			{
				continue;
			}

			boolean found = false;
			for (int i = 0; i < result.getHeaderSize(); i++)
			{
				if (result.getHeader(i).equals(name))
				{
					found = true;
					break;
				}
			}

			if (!found)
			{
				result.addColumns(name);
			}

			result.changeValue(name, size - 1, value);
		}

		super.setResult(null);
	}
}
