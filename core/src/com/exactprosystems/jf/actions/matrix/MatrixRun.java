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
import com.exactprosystems.jf.common.Context;
import com.exactprosystems.jf.common.MatrixRunner;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.parser.Parameters;
import com.exactprosystems.jf.common.parser.items.ActionItem.HelpKind;
import com.exactprosystems.jf.common.report.ReportBuilder;

import java.io.File;
import java.util.Date;

@ActionAttribute(
		group					= ActionGroups.Matrix,
		suffix					= "MXRN",
		generalDescription 		= "Starts new matrix at the desired time.",
		additionFieldsAllowed 	= false,
		outputDescription 		= "Special object that is connected with run matrix.",
		outputType				= MatrixRunner.class
	)
public class MatrixRun extends AbstractAction 
{
	public final static String atName = "At";
	public final static String matrixName = "Matrix";
	public final static String parameterName = "Parameter";
	public final static String reportSuffixName = "ReportSuffix";

	@ActionFieldAttribute(name = atName, mandatory = false, description = "Time to start the matrix.")
	protected Date at		= null;

	@ActionFieldAttribute(name = matrixName, mandatory = true, description = "Location of the matrix.")
	protected String matrix	= null;

	@ActionFieldAttribute(name = parameterName, mandatory = false, description = "Parameter for the matrix.")
	protected Object parameter	= null;

	@ActionFieldAttribute(name = reportSuffixName, mandatory = false, description = "Suffix that will be added to the report name.")
	protected String reportSuffix	= null;

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
		try(Context cloneContext = context.clone() )
		{
			MatrixRunner runner = new MatrixRunner(cloneContext, new File(this.matrix), this.at, this.parameter);
			runner.setReportSuffix(this.reportSuffix);
			runner.start();
			super.setResult(runner);
		}

	}
}
