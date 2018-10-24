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
import com.exactprosystems.jf.common.CommonHelper;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.DocumentKind;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Parameters;
import com.exactprosystems.jf.functions.HelpKind;

import java.io.Reader;
import java.util.Date;

@ActionAttribute(
		group					   = ActionGroups.Matrix,
		suffix					   = "MXRN",
		constantGeneralDescription = R.MATRIX_RUN_GENERAL_DESC,
		additionFieldsAllowed 	   = false,
		constantOutputDescription  = R.MATRIX_RUN_OUTPUT_DESC,
		outputType				   = MatrixConnection.class,
		constantExamples 		   = R.MATRIX_RUN_EXAMPLE,
		seeAlsoClass 			   = {MatrixRunFromText.class, MatrixWait.class}
	)
public class MatrixRun extends AbstractAction 
{
	public static final String atName        = "At";
	public static final String matrixName    = "Matrix";
	public static final String parameterName = "Parameter";

	@ActionFieldAttribute(name = matrixName, mandatory = true, constantDescription = R.MATRIX_RUN_MATRIX)
	protected String matrix;

	@ActionFieldAttribute(name = atName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MATRIX_RUN_AT)
	protected Date at;

	@ActionFieldAttribute(name = parameterName, mandatory = false, def = DefaultValuePool.Null, constantDescription = R.MATRIX_RUN_PARAMETER)
	protected Object parameter;

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
		try (Reader reader = CommonHelper.readerFromFileName(this.matrix))
		{
			Matrix matrix = (Matrix) context.getFactory().createDocument(DocumentKind.MATRIX, this.matrix);
			matrix.load(reader);
			matrix.setOut(context.getOut());
			MatrixConnection connection = matrix.start(this.at, this.parameter);
			super.setResult(connection);
		}
	}
}
