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
import com.exactprosystems.jf.functions.Text;

import java.io.Reader;
import java.io.StringReader;

@ActionAttribute(
		group					= ActionGroups.Text,
		suffix					= "TXT",
		generalDescription 		=
 "The purpose of the action is to create the object type {{$Text$}}. "
+ "The object type {{$Text$}} is the text-based pattern which consists of lines. "
+ "Object type {{$Text$}} is used as the input object in many text actions. "
+ "Also the object type {{$Text$}} can be converted into the line object "
+ "which can contain many lines {{$Text.Out.toString()$}} divided by the line break symbol (\\n) and "
+ "transferred to any action which accepts the line as the parameter or parameters, for example in action {{@SQLExecute@}}.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "The object type {{$Text$}} is the text-based pattern which consists of lines.",
		outputType				= Text.class,
		examples =
 "{{##Id;#Action;#Content\n"
+ "TXT1;TextCreate;'Text'\n"
+ "#Assert;#Message\n"
+ "TXT1.Out.toString() == 'Text';#}}\n",
		seeAlso = "{{@TextReport@}}, {{@TextAddLine@}}, {{@TextLoadFromFile@}}, {{@TextPerform@}}, {{@TextSaveToFile@}}," +
				" {{@TextSetValue@}}",
		seeAlsoClass = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextPerform.class,
		TextSaveToFile.class, TextSetValue.class}
	)
public class TextCreate extends AbstractAction 
{
	public final static String contentName = "Content";

	@ActionFieldAttribute(name = contentName, mandatory = true, description = "The text which is required to type.")
	protected String 	content 	= null;

	public TextCreate()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try (Reader reader = new StringReader(this.content))
		{
			Text text = new Text(reader);
			super.setResult(text);
		}
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}

