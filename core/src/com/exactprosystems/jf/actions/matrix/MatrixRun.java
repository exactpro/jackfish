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
import com.exactprosystems.jf.tool.custom.console.ConsoleText;
import com.exactprosystems.jf.tool.matrix.TabConsole;

import java.io.File;
import java.io.FileReader;
import java.io.PrintStream;
import java.io.Reader;
import java.util.Date;

@ActionAttribute(
		group					= ActionGroups.Matrix,
		suffix					= "MXRN",
		generalDescription 		= "The purpose of the action is to run a Matrix from a file. An execution context"
				+ " is created for the run matrix, so as matrices do not cross under the run. The run matrix creates"
				+ " its own status report. The run matrix uses the same output console as the triggering one. Otherwise,"
				+ " messages sent by action {{@Print@}} would be invisible. To pass some value to the run matrix,"
				+ " {{$Parameter$}} should be used. To pass several values at once, they should be bulked, for example,"
				+ " in Map. In the started matrix to access this value use public variable with the predefined"
				+ " name {{$Parameter$}}.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "A special object which identifies the started matrix. This object is required"
				+ " for {{@MatrixWait@}} action to wait when the started matrix stops. With the help of this object"
				+ " property one can access information about the number of successfully run and failed test cases"
				+ " of the started matrix.",
		outputType				= MatrixRunner.class,
		examples =
				"{{##Id;#Action;#Matrix\n" +
				"MXRN1;MatrixRun;'matrices/Matrix.jf'\n" +
				"#Assert;#Message\n" +
				"MXRN1.Out.isRunning();'MatrixRun is failed'#}}",
		seeAlso = "{{@MatrixRunFromText@}}, {{@MatrixWait@}}",
		seeAlsoClass = {MatrixRunFromText.class, MatrixWait.class}
	)
public class MatrixRun extends AbstractAction 
{
	public final static String atName = "At";
	public final static String matrixName = "Matrix";
	public final static String parameterName = "Parameter";

	@ActionFieldAttribute(name = matrixName, mandatory = true, description = "A file path to the run matrix. "
			+ "A path can be both absolute and relative. A current folder, with respect to which a file location"
			+ " is specified, is a folder where the configuration is.")
	protected String matrix	= null;
	
	@ActionFieldAttribute(name = atName, mandatory = false, description = "Is used to state the time when"
			+ " the matrix is started. If the specified time is not yet, the launched matrix goes the halted"
			+ " state before the start time, otherwise, the matrix starts straightaway.")
	protected Date at;

	@ActionFieldAttribute(name = parameterName, mandatory = false, description = "Is used to pass parameters"
			+ " to the started matrix. As a call – by – reference mechanism is used, be careful – the started matrix"
			+ " could modify the object passed. It should be considered and can be used for a feedback mechanism between matrices.")
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
		switch (fieldName)
		{
			case atName:
				return HelpKind.ChooseDateTime;
			
			case matrixName:
				return HelpKind.ChooseOpenFile;
		}
		return null;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
        try(Context newContext = context.getFactory().createContext();
			Reader reader = new FileReader(new File(this.matrix)))
		{
			MatrixRunner runner = newContext.createRunner(this.matrix, reader, this.at, this.parameter);
            newContext.setOut(context.getOut());
            runner.start();
			super.setResult(runner);
		}
	}
}
