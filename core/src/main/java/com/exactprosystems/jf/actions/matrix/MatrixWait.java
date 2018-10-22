/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
	protected MatrixConnection matrix;

	@ActionFieldAttribute(name = timeName, mandatory = false, def = DefaultValuePool.Long0, constantDescription = R.MATRIX_WAIT_TIME)
	protected Long time;

	@Override
	public void doRealAction(Context context, ReportBuilder report, Parameters parameters, AbstractEvaluator evaluator) throws Exception
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
