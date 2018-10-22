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

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;

import java.util.List;

@ActionAttribute(
		group 					   = ActionGroups.XML,
		suffix 					   = "XML",
		constantGeneralDescription = R.XML_CHILDREN_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_CHILDREN_OUTPUT_DESC,
		outputType 				   = List.class,
		constantExamples 		   = R.XML_CHILDREN_EXAMPLE
	)

public class XmlChildren extends AbstractAction
{
	public static final String xmlName = "Xml";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_CHILDREN_XML)
	protected Xml xml;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.xml.getChildren());
	}
}
