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
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

import java.io.Reader;

@ActionAttribute(
		group					   = ActionGroups.Text,
		suffix				       = "TXT",
		constantGeneralDescription = R.TEXT_CREATE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TEXT_CREATE_OUTPUT_DESC,
		outputType				   = Text.class,
		constantExamples 		   = R.TEXT_CREATE_EXMAPLE,
		seeAlsoClass 			   = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextPerform.class,
		TextSaveToFile.class, TextSetValue.class}
	)
public class TextCreate extends AbstractAction 
{
	public static final String contentName = "Content";

	@ActionFieldAttribute(name = contentName, mandatory = true, constantDescription = R.TEXT_CREATE_CONTENT)
	protected String content;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try (Reader reader = CommonHelper.readerFromString(this.content))
		{
			super.setResult(new Text(reader));
		}
	}
}

