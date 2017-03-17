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

@ActionAttribute(
		group					= ActionGroups.Text,
		generalDescription 		=
 "The purpose of the action is to change the definite line in the object {{$Text$}}. "
+ "The object type {{$Text$}} is the text-based pattern which consists of lines. "
+ "It can be used when it is required to make changes in the object, which has a "
+ "sufficiently large volume or when there is no access to the source "
+ "from which it was obtained.",
		additionFieldsAllowed 	= false,
		examples =
 "{{##Id;#Action;#Content\n"
+ "TXT1;TextCreate;'Text'\n"
+ "\n"
+ "#Action;#Line;#Text;#Index\n"
+ "TextSetValue;'string successfully set';TXT1.Out;2#}}",
		seeAlso = "{{@TextReport@}}, {{@TextAddLine@}}, {{@TextLoadFromFile@}}, {{@TextCreate@}}, {{@TextSaveToFile@}}," +
				" {{@TextPerform@}}",
		seeAlsoClass = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextCreate.class, TextSaveToFile.class, TextPerform.class}
	)
public class TextSetValue extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String lineName = "Line";
	public final static String indexName = "Index";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Object {{$Text$}}, in which" +
			" it is necessary to change the line.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = lineName, mandatory = true, description = "Input line.")
	protected String	line 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = true, description = "Line index, which is required to change. ")
	protected Integer	index 	= 0;

	public TextSetValue()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		text.set(this.index, this.line);
		
		super.setResult(null);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}

