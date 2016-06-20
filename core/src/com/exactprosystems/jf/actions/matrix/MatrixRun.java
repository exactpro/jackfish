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
import com.exactprosystems.jf.documents.matrix.parser.items.ActionItem.HelpKind;

import java.io.File;
import java.io.FileReader;
import java.io.Reader;
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

	@ActionFieldAttribute(name = matrixName, mandatory = true, description = "Location of the matrix.")
	protected String matrix	= null;
	
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
		
		
		try(Context cloneContext = context.clone();
			Reader reader = new FileReader(new File(this.matrix));)
		{
			//TODO need use constructor matrixrunner with file, not reader. bug #35073
			MatrixRunner runner = cloneContext.createRunner(reader, this.at, this.parameter);
			runner.start();
			super.setResult(runner);
		}

	}


}
