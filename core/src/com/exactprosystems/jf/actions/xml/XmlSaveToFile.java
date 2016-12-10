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
		generalDescription 		= "Save XML object to a file.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if saving is successfull.",
		outputType				= Boolean.class
	)
public class XmlSaveToFile extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String fileNameName = "File";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "XML object.")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, description = "File name.")
	protected String 	fileName 	= null;

	public XmlSaveToFile()
	{
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return fileNameName.equals(fieldName) ? HelpKind.ChooseSaveFile : null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.xml.save(this.fileName));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
