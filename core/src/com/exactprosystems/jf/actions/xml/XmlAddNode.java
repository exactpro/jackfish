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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ActionAttribute(
		group					= ActionGroups.XML,
		generalDescription 		= "The purpose of the action is to add a new element at the end of the parental element"
				+ " Xml structure. It can be applied when necessary changes have to be done to the current object."
				+ "If a NewXml parameter is indicated then the parameters {{$NodeName$}} and {{$Content$}} are ignored.",
		additionFieldsAllowed 	= true,
		additionalDescription   = "With the help of additional parameters one can transfer attributes for the new node. "
				+ "The name of the parameter contains the name of an attribute, in the value of a parameter the value of "
				+ "an attribute is specified. It is used when the insertion of the node is accompanied by the usage of "
				+ "NodeName and Content parameters.",
		examples = "{{`Example #1:`}}"
				+ "{{`1. Create an Xml structure by loading a file.`}}"
				+ "{{`Contents of an xml file: `}}"
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
				+ "{{`2. Add node to it by giving it the name and attribute ‘id’ and the meaning ‘gift’`}}"
				+ "{{`3. Make sure that node has been added and contains the transferred attribute.`}}"
				+ "\n"
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'Xml.xml'\n"
				+ "\n"
				+ "\n"
				+ "#Action;#NodeName;#Xml;#id\n"
				+ "XmlAddNode;'ps';XML1.Out;'gift'\n"
				+ "\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "XML1.Out.getChild('ps').getAttribute('id') == 'gift';'No such attribute'#}}"
				+ "\n"
				+ "\n"
				+ "{{`Example #2:`}}"
				+ "\n"
				+ "{{`1. Create object Xml similar to the object from the first example.`}}"
				+ "{{`2. Create second Xml object of the following content:`}} "
				+ "{{#<ps id=\"gift\">\n"
				+ "<body>P.S. I have a gift for you...</body>\n"
				+ "</ps>#}} "
				+ "{{`3. Add to it Xml structure applying parameter NewXml.`}}"
				+ "{{`4. Make sure that the structure has been added and contains the transferred attribute.`}}"
				+ "\n"
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'Xml.xml'\n"
				+ "\n"
				+ "\n"
				+ "#Id;#Action;#File\n"
				+ "XML2;XmlLoadFromFile;'Xml2.xml'\n"
				+ "\n"
				+ "\n"
				+ "#Action;#NewXML;#NodeName;#Xml\n"
				+ "XmlAddNode;XML2.Out;;XML1.Out\n"
				+ "\n"
				+ "\n"
				+ "#Assert;#Message\n"
				+ "XML1.Out.getChild('ps').getAttribute('id') == 'gift';'No such attribute'#}}"
	)
public class XmlAddNode extends AbstractAction 
{
	public final static String xmlName 		= "Xml";
	public final static String nodeNameName = "NodeName";
	public final static String contentName 	= "Content";
	public final static String newXML = "NewXML";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "An Xml structure to which an action has to be done.")
	protected Xml 		xml 	= null;

	@ActionFieldAttribute(name = nodeNameName, mandatory = true, description = "The name of an added element.")
	protected String 	nodeName 	= null;

	@ActionFieldAttribute(name = contentName, mandatory = false, description = "The contents of a new element.")
	protected String 	content;

	@ActionFieldAttribute(name =  newXML, mandatory = false, description = "Node that has to be added to the original "
			+ "structure. If it is indicated then parameters NodeName and Content are ignored.")
	protected Xml 		copiedXML;

	public XmlAddNode()
	{
	}

	@Override
	public void initDefaultValues() 
	{
		content 	= null;
		copiedXML 	= null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (copiedXML == null)
		{
			this.xml.addNode(this.nodeName, this.content, parameters.select(TypeMandatory.Extra).makeCopy());
		}
		else
		{
			this.xml.addNode(copiedXML);
		}
		super.setResult(null);
	}
}