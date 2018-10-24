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
import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameter;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;

@ActionAttribute(
		group						  = ActionGroups.System,
		constantGeneralDescription    = R.PRINT_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.PRINT_ADDITIONAL_DESC,
		constantExamples      		  = R.PRINT_EXAMPLE,
		seeAlsoClass 				  = {Show.class}
	)
public class Print extends AbstractAction 
{
	public static final String strName = "Str";

	@ActionFieldAttribute(name = strName, mandatory = false, def = DefaultValuePool.EmptyString, constantDescription = R.PRINT_MESSAGE)
	protected String message;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		StringBuilder sb = new StringBuilder();
		if (!Str.IsNullOrEmpty(this.message))
		{
			sb.append(this.message);
			sb.append('\t');
		}

		for (Parameter parameter : parameters.select(TypeMandatory.Extra))
		{
			sb.append(parameter.getName());
			sb.append(" = ");
			sb.append(parameter.getValue());
			sb.append('\t');
		}
		context.getOut().println(sb.toString());

		super.setResult(null);
	}
}
