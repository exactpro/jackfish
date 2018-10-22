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

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
        group                      = ActionGroups.Text,
        constantGeneralDescription = R.TEXT_ADD_LINE_GENERAL_DESC,
        additionFieldsAllowed      = false,
        constantExamples           = R.TEXT_ADD_LINE_EXAMPLE,
        seeAlsoClass               = {TextReport.class, TextPerform.class, TextLoadFromFile.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
)
public class TextAddLine extends AbstractAction
{
	public static final String textName = "Text";
	public static final String lineName = "Line";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_ADD_LINE_TEXT)
	protected Text text;

	@ActionFieldAttribute(name = lineName, mandatory = true, constantDescription = R.TEXT_ADD_LINE_LINE)
	protected String line;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.text.add(this.line);
		super.setResult(null);
	}
}

