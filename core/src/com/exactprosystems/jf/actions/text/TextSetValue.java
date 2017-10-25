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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

@ActionAttribute(
		group					   = ActionGroups.Text,
		constantGeneralDescription = R.TEXT_SET_VALUE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples           = R.TEXT_SET_VALUE_EXAMPLE,
		seeAlsoClass 			   = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextCreate.class,
		TextSaveToFile.class, TextPerform.class}
	)
public class TextSetValue extends AbstractAction 
{
	public final static String textName = "Text";
	public final static String lineName = "Line";
	public final static String indexName = "Index";

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.TEXT_SET_VALUE_TEXT)
	protected Text 	text 	= null;

	@ActionFieldAttribute(name = lineName, mandatory = true, constantDescription = R.TEXT_SET_VALUE_LINE)
	protected String	line 	= null;

	@ActionFieldAttribute(name = indexName, mandatory = true, constantDescription = R.TEXT_SET_VALUE_INDEX)
	protected Integer	index 	= 0;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (index < 0 || index >= this.text.size())
		{
			super.setError("Index '" + index + "' is out of bounds", ErrorKind.WRONG_PARAMETERS);
			return;
		}

		text.set(this.index, this.line);

		super.setResult(null);
	}
}

