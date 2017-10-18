////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
		suffix					   = "XML",
		constantGeneralDescription = R.XML_SELECT_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_SELECT_OUTPUT_DESC,
		outputType				   = Xml.class,
		constantExamples 		   = R.XML_SELECT_EXAMPLE
	)
public class XmlSelect extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String nodeNameName = "NodeName";
	public final static String xpathName = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure in which the element needs to be detected.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = nodeNameName, mandatory = true, description = "The name of a core element to which the "
			+ "collection of the found elements will be allocated.")
	protected String 	nodeName 	= null;

	@ActionFieldAttribute(name = xpathName, mandatory = true, description = "The Xpath, the path to the element.")
	protected String 	xpath 	= null;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.xml.createListByXpath(this.nodeName, this.xpath));
	}
}

