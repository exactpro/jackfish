/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
