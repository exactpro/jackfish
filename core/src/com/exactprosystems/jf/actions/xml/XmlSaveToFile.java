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
		generalDescription 		= "The purpose of this action is to keep (store) of the transferred Xml structure in the assigned file.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True, if saved successfully.",
		outputType				= Boolean.class,
		examples 				= "{{`1. Save the Xml object into the file filename.xml`}}"
				+ "{{`2. Make sure that the check went well.`}} "
				+ "{{#\n" +
				"#Id;#Action;#Xml;#File\n"
				+ "XSTF;XmlSaveToFile;Xml1;’path/filename.xml’\n"
				+ "#Assert;#Message\n"
				+ "XSTF.Out;'File was not saved'#}}"
	)
public class XmlSaveToFile extends AbstractAction 
{
	public final static String xmlName = "Xml";
	public final static String fileNameName = "File";

	@ActionFieldAttribute(name = xmlName, mandatory = true, description = "The Xml structure that needs to be kept (preserved).")
	protected Xml 	xml 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, description = "The path to the file. If the given path "
			+ "already contains a file then it will be replaced.")
	protected String 	fileName 	= null;

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
}
