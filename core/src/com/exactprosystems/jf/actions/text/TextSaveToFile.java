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
		generalDescription 		= "Save text object to a file.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "True if saving is successfull.",
		outputType				= Boolean.class
	)
public class TextSaveToFile extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String fileNameName = "File";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "The text object.")
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, description = "File name.")
	protected String 	fileName 	= null;

	public TextSaveToFile()
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
		super.setResult(this.text.save(this.fileName));
	}
}
