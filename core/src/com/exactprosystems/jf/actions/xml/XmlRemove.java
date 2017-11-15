////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
		constantGeneralDescription = R.XML_REMOVE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.XML_REMOVE_EXAMPLE
	)
public class XmlRemove extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String xpathName = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_REMOVE_XML)
	protected Xml 	xml;

	@ActionFieldAttribute(name = xpathName, mandatory = true, constantDescription = R.XML_REMOVE_XPATH)
	protected String 	xpath;
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.xml.removeByXpath(this.xpath);
		super.setResult(null);
	}
}

