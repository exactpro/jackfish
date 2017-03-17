////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.text;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					= ActionGroups.Text,
		suffix					= "TXT",
		generalDescription 		=
 "The purpose of the action is to create the object type {{$Text$}} from the file. "
+ "The object type {{$Text$}} is the text-based pattern which consists of lines. "
+ "Any file which contains plain-text can be used as a source file.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "The object type {{$Text$}} is the text-based pattern which consists of lines.",
		outputType				= Text.class,
		examples =
 "{{##Id;#Action;#File\n"
+ "TXT1;TextLoadFromFile;’path/text.txt’\n"
+ "\n"
+ "#Assert;#Message\n"
+ "TXT1.Out != null;#}}\n",
		seeAlso = "{{@TextReport@}}, {{@TextAddLine@}}, {{@TextPerform@}}, {{@TextCreate@}}, {{@TextSaveToFile@}}," +
				" {{@TextSetValue@}}",
		seeAlsoClass = {TextReport.class, TextAddLine.class, TextPerform.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
	)
public class TextLoadFromFile extends AbstractAction 
{
	public final static String fileName 		= "File";

	@ActionFieldAttribute(name = fileName, mandatory = true, description = "A text file, the text of which is required to type")
	protected String 	file 	= null;

	public TextLoadFromFile()
	{
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context,	Parameters parameters, String fieldName) throws Exception
	{
		return fileName.equals(fieldName) ? HelpKind.ChooseOpenFile : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Text(this.file));
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}
