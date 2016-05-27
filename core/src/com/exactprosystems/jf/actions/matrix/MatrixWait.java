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

@ActionAttribute(
		group					= ActionGroups.Matrix,
		suffix					= "MXWT",
		generalDescription 		= "Waits when the matrix stops.",
		additionFieldsAllowed 	= false
	)
public class MatrixWait extends AbstractAction 
{
	public final static String matrixName = "Matrix";
	public final static String timeName = "Time";

	@ActionFieldAttribute(name = matrixName, mandatory = true, description = "Special object that is connected with run matrix.")
	protected MatrixRunner matrix	= null;

	@ActionFieldAttribute(name = timeName, mandatory = false, description = "Timeout.")
	protected long time;

	@Override
	public void initDefaultValues() 
	{
		time	= 0L;
	}
	
	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception 
	{
		if (this.matrix == null)
		{
			super.setError("The matrix object is null");
		}
		else
		{
			this.matrix.join(this.time);
			super.setResult(null);
		}
	}
}
