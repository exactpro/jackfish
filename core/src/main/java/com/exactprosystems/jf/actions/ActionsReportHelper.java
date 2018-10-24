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

package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;

import java.util.List;
import java.util.stream.Collectors;

public class ActionsReportHelper
{
	private ActionsReportHelper()
	{

	}

	public static void fillListForParameter(Matrix matrix, List<ReadableValue> list, AbstractEvaluator evaluator)
	{
		List<String> listIds = matrix.listOfIds(TestCase.class);
		if (!listIds.isEmpty())
		{
			//empty string for reset value
			list.add(0, new ReadableValue(""));
			list.addAll(listIds.stream()
					.map(evaluator::createString)
					.map(ReadableValue::new)
					.collect(Collectors.toList())
			);
		}
	}

	public static String getBeforeTestCase(String testCaseId, Matrix matrix)
	{
		if (Str.IsNullOrEmpty(testCaseId))
		{
			return null;
		}
		return matrix.listOfIds(TestCase.class)
				.stream()
				.filter(testCaseId::equals)
				.findFirst()
				.orElse(null);
	}
}
