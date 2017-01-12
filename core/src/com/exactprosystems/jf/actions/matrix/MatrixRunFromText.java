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
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;
import com.exactprosystems.jf.functions.Text;

import java.io.Reader;
import java.io.StringReader;
import java.util.Date;

@ActionAttribute(
		group					= ActionGroups.Matrix,
		suffix					= "MXRN",
		generalDescription 		= "The purpose of this action is to run the matrix from the text. "
				+ "An execution context is created for the run matrix, so as matrices do not cross under run. "
				+ "The run matrix creates its own status report.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "A special object which identifies the started matrix. "
				+ "This object is required for {{@MatrixWait@}} action to wait when the started matrix stops."
				+ "With the help of this object property one can access information about the number of"
				+ " successfully run and failed test cases of the started matrix. ",
		outputType				= MatrixRunner.class,
		examples = "{{##Id;#Action;#Text\n" +
				"MXRN1;MatrixRunFromText;Text\n" +
				"\n" +
				"\n" +
				"#Assert;#Message\n" +
				"MXRN1.Out.isRunning();'MatrixRun is failed'#}}",
		seeAlso = "{{@MatrixRun@}}, {{@MatrixWait@}}"
	)
public class MatrixRunFromText extends AbstractAction 
{
	public final static String atName = "At";
	public final static String textName = "Text";
	public final static String parameterName = "Parameter";

	@ActionFieldAttribute(name = textName, mandatory = true, description = "{{$Text$}} object related to the execution matrix.")
	protected Text text	= null;

	@ActionFieldAttribute(name = atName, mandatory = false, description = "Is used to state the time when the matrix"
			+ " is started. If the specified time is not yet, the launched matrix goes the halted state before the"
			+ " start time, otherwise, the matrix starts straightaway.")
	protected Date at;

	@ActionFieldAttribute(name = parameterName, mandatory = false, description = "Is used to pass parameters to"
			+ " the started matrix. As a call – by – reference mechanism is used, be careful – the started"
			+ " matrix could modify the object passed.")
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
			MatrixRunner runner = contextClone.createRunner("new", reader, this.at, this.parameter);
			runner.start();
			
			super.setResult(runner);
		}
	}
}
