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
		generalDescription 		= "The purpose of this action is to delete all the elements appropriate for the given XPath.",
		additionFieldsAllowed 	= false,
		examples 				= "{{`1. Create an Xml object by downloading it from the file.`}}"
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
				+ "{{`2. Delete the element in transferring XPath.`}}"
				+ "{{`3. Make sure that the element has been deleted and its content is not in the report.`}} "
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "\n"
				+ "#Action;#Xpath;#Xml\n"
				+ "XmlRemove;'//from';XML1.Out\n"
				+ "\n"
				+ "#Action;#Xml;#Title\n"
				+ "XmlReport;XML1.Out;'Report Title'#}}"
	)
public class XmlRemove extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String xpathName = "Xpath";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure that needs to be changed (altered)")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = xpathName, mandatory = true, description = "Formula for the search of an element.")
	protected String 	xpath 	= null;

	public XmlRemove()
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
		xml.removeByXpath(this.xpath);
		super.setResult(null);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}

