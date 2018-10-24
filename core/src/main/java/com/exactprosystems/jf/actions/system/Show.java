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
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;

import java.util.List;

@ActionAttribute(
		group					   = ActionGroups.System,
		constantGeneralDescription = R.SHOW_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.SHOW_EXAMPLE,
		seeAlsoClass 			   = {Print.class}
	)
public class Show extends AbstractAction 
{
	public static final String notifierName = "Notifier";
	public static final String textName     = "Text";

	@ActionFieldAttribute(name = notifierName, mandatory = true, constantDescription = R.SHOW_NOTIFIER)
	protected Notifier notifier;

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.SHOW_TEXT)
	protected String text;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		if (notifierName.equals(fieldName))
		{
			return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		if (notifierName.equals(parameterToFill))
		{
			list.addAll(CommonHelper.convertEnumsToReadableList(Notifier.values()));
		}
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		context.getFactory().popup(this.text, this.notifier);
		super.setResult(null);
	}
}
