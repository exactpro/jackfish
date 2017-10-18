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
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.Text;

import java.io.Reader;
import java.io.StringReader;

@ActionAttribute(
		group					   = ActionGroups.Text,
		suffix				       = "TXT",
		constantGeneralDescription = R.TEXT_CREATE_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.TEXT_CREATE_OUTPUT_DESC,
		outputType				   = Text.class,
		constantExamples 		   = R.TEXT_CREATE_EXMAPLE,
		seeAlsoClass 			   = {TextReport.class, TextAddLine.class, TextLoadFromFile.class, TextPerform.class,
		TextSaveToFile.class, TextSetValue.class}
	)
public class TextCreate extends AbstractAction 
{
	public final static String contentName = "Content";

	@ActionFieldAttribute(name = contentName, mandatory = true, description = "The text which is required to type.")
	protected String 	content 	= null;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		try (Reader reader = CommonHelper.readerFromString(this.content))
		{
			Text text = new Text(reader);
			super.setResult(text);
		}
	}
}

