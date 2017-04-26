package com.exactprosystems.jf.actions;

import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.items.TestCase;

import java.util.List;
import java.util.stream.Collectors;

public class ActionsReportHelper
{
	public static void fillListForParameter(Matrix matrix, List<ReadableValue> list, AbstractEvaluator evaluator) throws Exception
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
		return matrix.listOfIds(TestCase.class).stream()
				.filter(st -> st.equals(testCaseId))
				.findFirst()
				.orElse(null);
	}
}
