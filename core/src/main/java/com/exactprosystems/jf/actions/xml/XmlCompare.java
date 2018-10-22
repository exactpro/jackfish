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

package com.exactprosystems.jf.actions.xml;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.XML,
		suffix					   = "XMLCMP",
		constantGeneralDescription = R.XML_COMPARE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_COMPARE_OUTPUT_DESC,
		outputType				   = Boolean.class,
		constantExamples 		   = R.XML_COMPARE_EXAMPLE
	)
public class XmlCompare extends AbstractAction 
{
	public static final String actualName          = "Actual";
	public static final String expectedName        = "Expected";
	public static final String ignoreNodeOrderName = "IgnoreNodeOrder";

	@ActionFieldAttribute(name = actualName, mandatory = true, constantDescription = R.XML_COMPARE_ACTUAL)
	protected Xml actual;

	@ActionFieldAttribute(name = expectedName, mandatory = true, constantDescription = R.XML_COMPARE_EXPECTED)
	protected Xml expected;

	@ActionFieldAttribute(name = ignoreNodeOrderName, mandatory = false, def = DefaultValuePool.False, constantDescription = R.XML_COMPARE_IGNORE_NODE_ORDER)
	protected Boolean ignoreNodesOrder;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (ignoreNodeOrderName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return super.howHelpWithParameterDerived(context, parameters, fieldName);
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (ignoreNodeOrderName.equals(parameterToFill))
		{
			list.add(ReadableValue.TRUE);
			list.add(ReadableValue.FALSE);
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.actual.compareTo(this.expected, this.ignoreNodesOrder));
	}
}
