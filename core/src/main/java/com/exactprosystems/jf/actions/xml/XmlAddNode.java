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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					      = ActionGroups.XML,
		constantGeneralDescription    = R.XML_ADD_NODE_GENERAL_DESC,
		additionFieldsAllowed 	   	  = true,
		constantAdditionalDescription = R.XML_ADD_NODE_ADDITIONAL_DESC,
		constantExamples 			  = R.XML_ADD_NODE_EXAMPLE
	)
public class XmlAddNode extends AbstractAction 
{
	public static final String xmlName      = "Xml";
	public static final String nodeNameName = "NodeName";
	public static final String contentName  = "Content";
	public static final String newXML       = "NewXML";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_ADD_NODE_XML)
	protected Xml xml;

	@ActionFieldAttribute(name = nodeNameName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_ADD_NODE_NODE_NAME)
	protected String nodeName;

	@ActionFieldAttribute(name = contentName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_ADD_NODE_CONTENT_NAME)
	protected String content;

	@ActionFieldAttribute(name = newXML, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_ADD_NODE_NEW_XML)
	protected Xml copiedXML;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.copiedXML == null)
		{
			if (this.nodeName == null)
			{
				setError(String.format("Parameter %s can't be null if the parameter %s is not null", nodeNameName, newXML), ErrorKind.EMPTY_PARAMETER);
				return;
			}
			this.xml.addNode(this.nodeName, this.content, parameters.select(TypeMandatory.Extra).makeCopy());
		}
		else
		{
			this.xml.addNode(copiedXML);
		}
		super.setResult(null);
	}
}