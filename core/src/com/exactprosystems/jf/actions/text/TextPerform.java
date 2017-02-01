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
        suffix					= "TXT",
        generalDescription 		=
 "The purpose of the action is to implement macro substitution in object {{$Text$}}. "
+ "The object type {{$Text$}} is the text-based pattern, which consists of lines, which can "
+ "contain macro substitution. Macro substitution is found directly in the test and "
+ "it represents the formula which is enclosed to special tag \"@{}\". The result of the "
+ "action work is the object {{$Text$}}, in which the results of the macro execution are presented "
+ "in the form of the literal text. For example: \"Hi! My name is @{personName}\" or \"My taxes "
+ "in 2016 is @{amount * 0.13}\"  will be converted into the text \"Hi! My name is John \" "
+ "and \"My taxes in 2016 is 13\" correspondingly, on condition that  the variable personName "
+ "contains \"John\", and the variable amount contains 100. It is recommended to use the following "
+ "action for a generation of the object {{$Text$}} during the matrix implementation and its upcoming "
+ "transfer to action {{@MatrixRunFromText@}} for a dynamic matrix execution or a text assembly of the "
+ "complex structure such as Expected value for the subsequent comparison with Actual value. "
+ "It is possible to find detailed information about macro options and macro executable "
+ "expressions on the website (access address).",
        additionFieldsAllowed 	= false,
        outputDescription 		= "The object type {{$Text$}} is the text-based pattern which consists of lines. ",
        outputType				= Text.class,
        examples                =
 "{{##Id;#Action;#Content\n"
+ "TXT1;TextCreate;'My taxes in 2016 is @{amount * 0.13} rub'\n"
+ "\n"
+ "\n"
+ "#Id;#Let\n"
+ "amount;100\n"
+ "\n"
+ "\n"
+ "#Id;#Action;#Text\n"
+ "TXT2;TextPerform;TXT1.Out\n"
+ "\n"
+ "\n"
+ "#Assert;#Message\n"
+ "TXT2.Out.toString() == 'My taxes in 2016 is 13 rub';'Assert failed'#}}\n",
        seeAlso = "{{@TextReport@}}, {{@TextAddLine@}}, {{@TextLoadFromFile@}}, {{@TextCreate@}}, {{@TextSaveToFile@}}," +
				" {{@TextSetValue@}}"
	)
public class TextPerform extends AbstractAction 
{
	public final static String textName = "Text";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Object {{$Text$}}, with which the operation is required to perform.")
	protected Text 	text 	= null;

	public TextPerform()
	{
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		Text result = text.perform(evaluator);
		
		super.setResult(result);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}

}

