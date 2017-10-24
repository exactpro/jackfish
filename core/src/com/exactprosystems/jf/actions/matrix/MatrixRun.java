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
import com.exactprosystems.jf.api.error.common.MatrixException;
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
	public final static String atName = "At";
	public final static String matrixName = "Matrix";
	public final static String parameterName = "Parameter";

	@ActionFieldAttribute(name = matrixName, mandatory = true, constantDescription = R.MATRIX_RUN_MATRIX)
	protected String matrix	= null;
	
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
        try(Reader reader = CommonHelper.readerFromFileName(this.matrix))
        {
            Matrix matrix = (Matrix)context.getFactory().createDocument(DocumentKind.MATRIX, this.matrix);
            matrix.load(reader);
            matrix.setOut(context.getOut());
            MatrixConnection connection = matrix.start(this.at, this.parameter); 
            super.setResult(connection);
        }
        catch (MatrixException matrixException)
        {
            super.setError(matrixException.getMessage(), matrixException.getErrorKind());
        }
	}
}
