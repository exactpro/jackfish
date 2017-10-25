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
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					   = ActionGroups.Text,
		constantGeneralDescription = R.TEXT_SAVE_TO_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TEXT_SAVE_TO_FILE_OUTPUT_DESC,
		outputType				   = Boolean.class,
		constantExamples 		   = R.TEXT_SAVE_TO_FILE_EXAMPLE,
		seeAlsoClass 			   = {TextReport.class, TextAddLine.class, TextLoadFromFile.class,
		TextCreate.class, TextPerform.class, TextSetValue.class}
	)
public class TextSaveToFile extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String fileNameName = "File";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_SAVE_TO_FILE_TEXT)
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = fileNameName, mandatory = true, constantDescription = R.TEXT_SAVE_TO_FILE_FILE)
	protected String 	fileName 	= null;

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
