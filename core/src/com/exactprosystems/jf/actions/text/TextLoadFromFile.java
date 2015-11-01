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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					= ActionGroups.Text,
		suffix					= "TXT",
		generalDescription 		= "Load text from a file as text.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Text object.",
		outputType				= Text.class
	)
public class TextLoadFromFile extends AbstractAction 
{
	public final static String fileName 		= "File";

	@ActionFieldAttribute(name = fileName, mandatory = true, description = "File name.")
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
}
