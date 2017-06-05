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
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					= ActionGroups.XML,
		suffix					= "XML",
		generalDescription 		= "The purpose of the action is to return (restore) the Xml structure allocated using "
				+ "Xpath parameter from Xml document. The first appropriate to the condition element and all its parts "
				+ "will be returned. The output value is a separate Xml structure and is not connected to the structure "
				+ "transferred in Xml parameter.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Xml structure containing the result of a demand.",
		outputType				= Xml.class,
		examples 				= "{{`1. Create Xml object by downloading it from the file.`}}"
				+ "{{`Contents of an xml file:`}} "
				+ "{{#<note> \n"
				+ "<to>\n"
				+ "<friend>\n"
				+ "<name id=\"first\">Tove</name>\n"
				+ "</friend>\n"
				+ "</to>\n"
				+ "<from>\n"
				+ "<friend>\n"
				+ "<name id=\"second\">Jani</name>\n"
				+ "</friend>\n"
				+ "</from>\n"
				+ "<heading>Reminder</heading>\n"
				+ "<body>Don't forget me this weekend!</body>\n"
				+ "</note>#}}"
				+ "\n"
				+ "{{`2. Find the first element from.`}} "
				+ "{{`3. Check the outcome`}} "
				+ "{{##Id;#Action;#File\n"
				+ "XML2;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "#Id;#Action;#Xpath;#Xml\n"
				+ "XML2;XmlFindFirst;'//name';XML1.Out\n"
				+ "#Assert;#Message\n"
				+ "XML2.Out.getChild().getAttribute() == 'first';'No such attribute'#}}"
	)
public class XmlSelectFirst extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String xpathName = "Xpath";
	public final static String nodeNameName = "NodeName";


	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure in which an operation needs to be done.")
	protected Xml 	xml;


	@ActionFieldAttribute(name = nodeNameName, mandatory = true, description = "Insert the selection result into assigned tag.")
	protected String 	nodeName;


	@ActionFieldAttribute(name = xpathName, mandatory = true, description = "The Xpath, the path to the element.")
	protected String 	xpath;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(xml.createOneByXpath(this.nodeName,this.xpath));
	}
}

