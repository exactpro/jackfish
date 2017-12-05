////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.actions.matrix;

import com.exactprosystems.jf.actions.*;
import com.exactprosystems.jf.api.common.MatrixConnection;
import com.exactprosystems.jf.api.common.i18n.R;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;

@ActionAttribute(
        group                      = ActionGroups.Matrix,
        constantGeneralDescription = R.MATRIX_WAIT_GENERAL_DESC,
        additionFieldsAllowed      = false,
        constantExamples           = R.MATRIX_WAIT_EXAMPLE,
        seeAlsoClass               = {MatrixRunFromText.class, MatrixRun.class}
)
public class MatrixWait extends AbstractAction
{
	public static final String matrixName = "Matrix";
	public static final String timeName   = "Time";

	@ActionFieldAttribute(name = matrixName, mandatory = true, constantDescription = R.MATRIX_WAIT_MATRIX)
	protected MatrixConnection matrix = null;

	@ActionFieldAttribute(name = timeName, mandatory = false, def = DefaultValuePool.Long0, constantDescription = R.MATRIX_WAIT_TIME)
	protected Long time;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
	{
		if (this.time == null)
		{
			super.setError("Column '" + timeName + "' can't be empty.", ErrorKind.EMPTY_PARAMETER);
			return;
		}

		boolean join = this.matrix.join(this.time);
		if (!join)
		{
			super.setError("The matrix doesn't finish during " + this.time + " ms", ErrorKind.TIMEOUT);
		}
		else
		{
			super.setResult(null);
		}
	}
}
