////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.matrix;

import com.exactprosystems.jf.actions.AbstractAction;
import com.exactprosystems.jf.actions.ActionAttribute;
import com.exactprosystems.jf.actions.ActionFieldAttribute;
import com.exactprosystems.jf.actions.ActionGroups;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.functions.Text;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

@ActionAttribute(
		group					= ActionGroups.Matrix,
		suffix					= "MXRN",
		generalDescription 		= "Starts new matrix at the desired time.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Special object that is connected with run matrix.",
		outputType				= MatrixRunner.class
	)
public class MatrixRunFromText extends AbstractAction 
{
	public final static String atName = "At";
	public final static String textName = "Text";
	public final static String parameterName = "Parameter";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "Text object.")
	protected Text text	= null;

	@ActionFieldAttribute(name = atName, mandatory = false, description = "Time to start the matrix.")
	protected Date at;

	@ActionFieldAttribute(name = parameterName, mandatory = false, description = "Parameter for the matrix.")
	protected Object parameter;
	
	@Override
	public void initDefaultValues() 
	{
		at			= null;
		parameter	= null;
	}
	
	@Override
	protected HelpKind howHelpWithParameterDerived(Context context, Parameters parameters, String fieldName) throws Exception
	{
		return atName.equals(fieldName) ? HelpKind.ChooseDateTime : null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		try (	Context contextClone =  context.clone();
				Reader reader = new StringReader(this.text.toString())	)
		{
			MatrixRunner runner = new MatrixRunner(contextClone, reader, this.at, this.parameter);
			runner.start();
			
			super.setResult(runner);
		}
	}
}
