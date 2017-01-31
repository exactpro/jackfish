////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.xml;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group 					= ActionGroups.XML, 
		suffix 					= "XML", 
		generalDescription 		= "The purpose of the action is to get the list of all the elements inside the transferred Xml."
				+ " Later this list can be used for iteration and search of elements using their index.",
		additionFieldsAllowed 	= false, 
		outputDescription 		= "Returns the list (type List object) of all the affiliated nodes of a transferred element."
				+ "Later this list can be used for iteration and search of elements using their index.",
		outputType 				= List.class,
		examples 				= "1. Create an Xml object by downloading it from the file.\n "
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
				+ "</note>#}} "
				+ "{{`2. Get a list of all affiliated elements.`}}"
				+ "{{`3. Make sure the list has been created and its size is more than 0.`}}"
				+ "\n"
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'Xml.xml'\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#Xml\n"
				+ "XML2;XmlChildren;XML1.Out#}}"
	)

public class XmlChildren extends AbstractAction
{
	public final static String	xmlName			= "Xml";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure to which an action needs to"
			+ " be applied. From the root of this structure a list of elements will be drawn.")
	protected Xml				xml				= null;

	public XmlChildren()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(xml.getChildren());
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}
