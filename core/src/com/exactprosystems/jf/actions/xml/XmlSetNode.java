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
import com.exactprosystems.jf.actions.DefaultValuePool;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.documents.matrix.parser.items.TypeMandatory;
import com.exactprosystems.jf.functions.Xml;

@ActionAttribute(
		group					      = ActionGroups.XML,
		constantGeneralDescription    = R.XML_SET_NODE_GENERAL_DESC,
		additionFieldsAllowed 	      = true,
		constantAdditionalDescription = R.XML_SET_NODE_ADDITIONAL_DESC,
		constantExamples              = R.XML_SET_NODE_EXAMPLE
	)
public class XmlSetNode extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String textName = "Text";

	@ActionFieldAttribute(name = xmlName, mandatory = true, constantDescription = R.XML_SET_NODE_XML)
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = textName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.XML_SET_NODE_TEXT)
	protected String 	text;

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

