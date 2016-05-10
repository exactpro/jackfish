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
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.TypeMandatory;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Xml;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@ActionAttribute(
		group					= ActionGroups.XML,
		generalDescription 		= "Adds new node into XML object.",
		additionFieldsAllowed 	= true
	)
public class XmlAddNode extends AbstractAction 
{
	public final static String xmlName 		= "Xml";
	public final static String nodeNameName = "NodeName";
	public final static String contentName 	= "Content";
	public final static String newXML = "NewXML";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "XML object.")
	protected Xml 		xml 	= null;

	@ActionFieldAttribute(name = nodeNameName, mandatory = true, description = "Name of XML object.")
	protected String 	nodeName 	= null;

	@ActionFieldAttribute(name = contentName, mandatory = false, description = "Content of XML object.")
	protected String 	content;

	@ActionFieldAttribute(name =  newXML, mandatory = false, description = "XML object to insert")
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