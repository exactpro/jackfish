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
		generalDescription 		= "The purpose of the action is to return (restore) the Xml structure, allocated using Xpath parameter from Xml document."
				+ "The first appropriate to the condition element and all its parts will be returned."
				+ "It is used when it is necessary to get a link to the part of a structure from the existing Xml document. "
				+ "The change of an output value of the action will lead to the change in Xml structure from which it was obtained.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Link to Xml structure that contains the search result.",
		outputType				= Xml.class,
		examples 				= "{{`1. Create an object by downloading it from the file.`}}"
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
				+ "{{`2. Find the first element name.`}}"
				+ "{{`3. Check the output.`}} "
				+ "{{##Id;#Action;#File\n"
				+ "XML2;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "#Id;#Action;#Xpath;#Xml\n"
				+ "XML2;XmlFindFirst;'//name';XML1.Out\n"
				+ "#Assert;#Message\n"
				+ "XML2.Out.getChild().getAttribute() == 'first';'No such attribute'#}}"
	)
public class XmlFindFirst extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String xpathName = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure in which the element needs to be detected.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = xpathName, mandatory = true, description = "The Xpath, the path to the element.")
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
		super.setResult(this.xml.findOneByXpath(this.xpath));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}

