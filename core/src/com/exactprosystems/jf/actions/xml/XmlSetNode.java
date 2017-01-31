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
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					= ActionGroups.XML,
		generalDescription 		= "The purpose of the action is to transfer the content of the core element of the Xml structure.",
		additionFieldsAllowed 	= true,
		additionalDescription 	= "With the help of additional parameters attributes for the the core node can be transferred."
				+ "Parameter is given the name of an attribute, the value of the parameter has the value of the attribute.",
		examples 				= "{{`1. Create an Xml object by downloading it from the file.`}}"
				+ "{{`2. Transfer to the core element content ‘Text for insert’`}}"
				+ "{{`3. Contents of an xml file:`}} "
				+ "{{#<note>"
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
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'/path/Xml.xml'\n"
				+ "\n"
				+ "#Id;#Action;#Text;#Xml\n"
				+ "XSN;XmlSetNode;'Text for insert';Xml#}}"
	)
public class XmlSetNode extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String textName = "Text";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "A type of Xml object to which an action needs to be applied.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = textName, mandatory = false, description = "Content which has to be transferred to the element.")
	protected String 	text;

	public XmlSetNode()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
		text 	= null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.text != null)
		{
			this.xml.setText(this.text);
		}
		
		this.xml.setAttributes(parameters.select(TypeMandatory.Extra).makeCopy());
		super.setResult(null);
	}
}

