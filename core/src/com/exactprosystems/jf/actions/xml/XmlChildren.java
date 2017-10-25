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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group 					   = ActionGroups.XML,
		suffix 					   = "XML",
		constantGeneralDescription = R.XML_CHILDREN_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.XML_CHILDREN_OUTPUT_DESC,
		outputType 				   = List.class,
		constantExamples 		   = R.XML_CHILDREN_EXAMPLE
	)

public class XmlChildren extends AbstractAction
{
	public final static String	xmlName			= "Xml";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_CHILDREN_XML)
	protected Xml				xml				= null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(xml.getChildren());
	}
}
