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

package com.exactprosystems.jf.actions.message;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.client.MapMessage;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
		group					   = ActionGroups.Messages,
		suffix					   = "MSGCMP",
		constantGeneralDescription = R.MESSAGE_COMPARE_TWO_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.MESSAGE_COMPARE_TWO_EXAMPLE
	)
public class MessageCompareTwo extends AbstractAction 
{
	public static final String actualName   = "Actual";
	public static final String expectedName = "Expected";
	public static final String excludeName  = "Exclude";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.MESSAGE_COMPARE_TWO_ACTUAL)
	protected MapMessage actual;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.MESSAGE_COMPARE_TWO_EXPECTED)
	protected MapMessage expected;

	@ActionFieldAttribute(name = excludeName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MESSAGE_COMPARE_TWO_EXCLUDE)
	protected String[] exclude;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		boolean res = this.actual.extendEquals(this.expected, this.exclude);
		if (res)
		{
			super.setResult(null);
		}
		else
		{
			super.setError("Messages are not equal.", ErrorKind.NOT_EQUAL);
		}
	}
}
