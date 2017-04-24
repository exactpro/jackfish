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
		generalDescription 		= "The purpose of this action is to download the XML structure from a file.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "XML structure.",
		outputType				= Xml.class,
		examples 				= "{{`1. Create Xml object by downloading it from the file.`}} "
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
				+ "{{`2. Make sure that the object has been created and contains nodes.`}} "
				+ "{{##Id;#Action;#File\n"
				+ "XML1;XmlLoadFromFile;'PathToTheFile'\n"
				+ "#Assert;#Message\n"
				+ "XML1.Result.toString() == 'Passed';'No such attribute'#}}"
	)
public class XmlLoadFromFile extends AbstractAction 
{
	public final static String fileName 		= "File";

	@ActionFieldAttribute(name = fileName, mandatory = true, description = "The path to the file.")
	protected String 	file 	= null;

	public XmlLoadFromFile()
	{
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return fileName.equals(fieldName) ? HelpKind.ChooseOpenFile : null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Xml(this.file));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
