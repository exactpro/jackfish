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
 "The purpose of the action is to add a line to the object type Text. "
+ "Object type Text is a text-based pattern which consists of lines. "
+ "The action can work for object creation Text line-by-line from different sources.",
		additionFieldsAllowed 	= true,
		examples =
"#Id;#Action;#Content\n"
+ "TXT1;TextCreate;'Text'\n"
+ "\n"
+ "\n"
+ "#Action;#Line;#Text\n"
+ "TextAddLine;'New line';TXT1.Out\n"
	)
public class TextAddLine extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String lineName = "Line";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Text(Text) - object Text.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = lineName, mandatory = true, description = "Line(String) — the line that should be added.")
	protected String	line 	= null;

	public TextAddLine()
	{
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		text.add(this.line);
		
		super.setResult(null);
	}

	@Override
	public void initDefaultValues() {
		// TODO Auto-generated method stub
		
	}
}

