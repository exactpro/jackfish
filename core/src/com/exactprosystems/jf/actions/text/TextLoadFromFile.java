/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
		group				 	   = ActionGroups.Text,
		suffix					   = "TXT",
		constantGeneralDescription = R.TEXT_LOAD_FROM_FILE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TEXT_LOAD_FROM_FILE_OUTPUT_DESC,
		outputType				   = Text.class,
		constantExamples 		   = R.TEXT_LOAD_FROM_FILE_EXAMPLE,
		seeAlsoClass = {TextReport.class, TextAddLine.class, TextPerform.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
	)
public class TextLoadFromFile extends AbstractAction 
{
	public static final String fileName = "File";

	@ActionFieldAttribute(name = fileName, mandatory = true, constantDescription = R.TEXT_LOAD_FROM_FILE_FILE)
	protected String file;

	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return fileName.equals(fieldName) ? HelpKind.ChooseOpenFile : null;
	}

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(new Text(this.file));
	}
}
