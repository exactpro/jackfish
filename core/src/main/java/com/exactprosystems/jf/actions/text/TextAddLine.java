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
        group                      = ActionGroups.Text,
        constantGeneralDescription = R.TEXT_ADD_LINE_GENERAL_DESC,
        additionFieldsAllowed      = false,
        constantExamples           = R.TEXT_ADD_LINE_EXAMPLE,
        seeAlsoClass               = {TextReport.class, TextPerform.class, TextLoadFromFile.class, TextCreate.class, TextSaveToFile.class, TextSetValue.class}
)
public class TextAddLine extends AbstractAction
{
	public static final String textName = "Text";
	public static final String lineName = "Line";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_ADD_LINE_TEXT)
	protected Text text;

	@ActionFieldAttribute(name = lineName, mandatory = true, constantDescription = R.TEXT_ADD_LINE_LINE)
	protected String line;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		this.text.add(this.line);
		super.setResult(null);
	}
}

