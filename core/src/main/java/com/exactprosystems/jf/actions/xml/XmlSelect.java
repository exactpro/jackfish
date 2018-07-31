/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					   = ActionGroups.XML,
		suffix					   = "XML",
		constantGeneralDescription = R.XML_SELECT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_SELECT_OUTPUT_DESC,
		outputType				   = Xml.class,
		constantExamples 		   = R.XML_SELECT_EXAMPLE
	)
public class XmlSelect extends AbstractAction 
{
	public static final String xmlName      = "Xml";
	public static final String nodeNameName = "NodeName";
	public static final String xpathName    = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_SELECT_XML)
	protected Xml xml;

	@ActionFieldAttribute(name = nodeNameName, mandatory = true, constantDescription = R.XML_SELECT_NODE_NAME)
	protected String nodeName;

	@ActionFieldAttribute(name = xpathName, mandatory = true, constantDescription = R.XML_SELECT_X_PATH)
	protected String xpath;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.xml.createListByXpath(this.nodeName, this.xpath));
	}
}

