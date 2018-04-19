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
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
        group					   = ActionGroups.Text,
        suffix					   = "TXT",
        constantGeneralDescription = R.TEXT_PERFORM_GENERAL_DESC,
        additionFieldsAllowed 	   = false,
        constantOutputDescription  = R.TEXT_PERFORM_OUTPUT_DESC,
        outputType				   = Text.class,
        constantExamples 		   = R.TEXT_PERFORM_EXAMPLE,
		seeAlsoClass = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
	)
public class TextPerform extends AbstractAction 
{
	public static final String textName = "Text";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_PERFORM_TEXT)
	protected Text text;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		super.setResult(this.text.perform(evaluator));
	}
}

