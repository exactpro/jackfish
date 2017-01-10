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
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Notifier;

@ActionAttribute(
		group					= ActionGroups.System,
		generalDescription 		= "The following action is needed to output values in the pop-up notification that appears above all windows.",
		additionFieldsAllowed 	= false,
		examples 				= "Output the following row \"Current time is: current_time\" in the pop-up window, "
				+ "with notification type Info.\n"
				+ "\n"
				+ "{{##Action;#Text;#Notifier\n"
				+ "Show;'Current time is: ' + DateTime.currentTime();Notifier.Info#}}",
		seeAlso = "{{@Print@}}"
	)
public class Show extends AbstractAction 
{
	public final static String notifierName = "Notifier";
	public final static String textName = "Text";

	@ActionFieldAttribute(name = notifierName, mandatory = true, description = "Set a type of notification: Info – blue color, Success – green, Error – red.")
	protected Notifier notifier; 

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Output row.")
	protected String text; 
	
	public Show()
	{
	}
	
	@Override
	public void initDefaultValues() 
	{
	}
	
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
