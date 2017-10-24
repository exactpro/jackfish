////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.system;

import java.util.List;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.actions.ReadableValue;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;

@ActionAttribute(
		group					   = ActionGroups.System,
		constantGeneralDescription = R.SHOW_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantExamples 		   = R.SHOW_EXAMPLE,
		seeAlsoClass 			   = {Print.class}
	)
public class Show extends AbstractAction 
{
	public final static String notifierName = "Notifier";
	public final static String textName = "Text";

	@ActionFieldAttribute(name = notifierName, mandatory = true, constantDescription = R.SHOW_NOTIFIER)
	protected Notifier notifier; 

	@ActionFieldAttribute(name = textName, mandatory = true, constantDescription = R.SHOW_TEXT)
	protected String text; 
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		switch (fieldName)
		{
			case notifierName:
				return HelpKind.ChooseFromList;
		}
		return null;
	}

	@Override
	protected void listToFillParameterDerived(List<ReadableValue> list, Context context, String parameterToFill, Parameters parameters) throws Exception
	{
		switch (parameterToFill)
		{
			case notifierName:
				list.add(new ReadableValue("Notifier.Error"));
				list.add(new ReadableValue("Notifier.Info"));
				list.add(new ReadableValue("Notifier.Success"));
				break;
		}
	}

	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.notifier == null)
		{
			super.setError("Notifier is null", ErrorKind.EMPTY_PARAMETER);
			return;
		}
		context.getFactory().popup(this.text, this.notifier);
		
		super.setResult(null);
	}
}
