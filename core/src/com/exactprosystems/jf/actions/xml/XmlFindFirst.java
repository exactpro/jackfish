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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					= ActionGroups.XML,
		suffix					= "XML",
		generalDescription 		= "Finds first suitable node of XML object by xpath",
		additionFieldsAllowed 	= false,
		outputDescription 		= "XML structure.",
		outputType				= Xml.class
	)
public class XmlFindFirst extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String xpathName = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "XML object.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = xpathName, mandatory = true, description = "Xpath.")
	protected String 	xpath 	= null;

	public XmlFindFirst()
	{
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(xml.findOneByXpath(this.xpath));
	}

}

