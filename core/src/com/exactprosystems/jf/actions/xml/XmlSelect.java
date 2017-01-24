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
				+ "Xpath parameter from Xml document. The action returns all appropriate to the condition elements "
				+ "(an element) and all their (its) parts. The output value is a separate Xml structure and is not connected"
				+ " to the structure transferred in Xml parameter.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Xml structure that contains the result of the demand.",
		outputType				= Xml.class,
		examples 				= "{{`Create an Xml object by downloading it from the file.`}}"
				+ "{{`Contents of an xml file:`}}"
				+ "{{#<note> \n"
				+ "<to>\n"
				+ "<friend>\n"
				+ "<name id=\"first\">Tove</name>\n"
				+ "</friend>\n"
				+ "</to>\n"
				+ "\n"
				+ "<from>\n"
				+ "<friend>\n"
				+ "<name id=\"second\">Jani</name>\n"
				+ "</friend>\n"
				+ "</from>\n"
				+ "<heading>Reminder</heading>\n"
				+ "<body>Don't forget me this weekend!</body>\n"
				+ "</note>#}}"
				+ " \n"
				+ "{{`2. Find the element from.`}}"
				+ "{{`3. Make sure that a new Xml structure has been created.`}}"
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#Xml;#Xpath;#NodeName\n"
				+ "XS;XmlSelect;Xml1;’//from;’sub_item’\n"
				+ "\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "XS.Out != null;'Object is null'#}}"
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

	public XmlSelect()
	{
	}

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return xpathName.equals(fieldName) ? HelpKind.BuildXPath : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(xml.createListByXpath(this.nodeName, this.xpath));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}

