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
import com.exactprosystems.jf.actions.DefaultValuePool;
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
    public final static String matrixName = "Matrix";
    public final static String timeName   = "Time";

    @ActionFieldAttribute(name = matrixName, mandatory = true, description = "A special object that connects to the"
            + " matrix which is run.")
    protected MatrixConnection    matrix     = null;

    @ActionFieldAttribute(name = timeName, mandatory = false, def = DefaultValuePool.Long0, description = "Matrix timeout per millisecond."
            + "If the matrix doesn't stop during the specified timeout, an action is failed."
            + "If the value is 0 - there will be pending before the matrix stops with no time limit."
            + "Be careful with such a value, if the started matrix is cycled, the current matrix will hang up in latency.")
    protected Long             time;

    @Override
    public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator)
            throws Exception
    {
        if(this.time == null)
        {
            super.setError("Column '" + timeName + "' can't be empty.", ErrorKind.EMPTY_PARAMETER);
            return;
        }

        if (this.matrix == null)
        {
            super.setError("The matrix object is null", ErrorKind.EMPTY_PARAMETER);
        }
        else
        {
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
}
