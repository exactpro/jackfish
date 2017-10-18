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

	@ActionFieldAttribute(name = matrixName, mandatory = true, description = "A file path to the run matrix. "
			+ "A path can be both absolute and relative. A current folder, with respect to which a file location"
			+ " is specified, is a folder where the configuration is.")
	protected String matrix	= null;
	
	@ActionFieldAttribute(name = atName, mandatory = false, def = DefaultValuePool.Null, description = "Is used to state the time when"
			+ " the matrix is started. If the specified time is not yet, the launched matrix goes the halted"
			+ " state before the start time, otherwise, the matrix starts straightaway.")
	protected Date at;

	@ActionFieldAttribute(name = parameterName, mandatory = false, def = DefaultValuePool.Null, description = "Is used to pass parameters"
			+ " to the started matrix. As a call - by - reference mechanism is used, be careful - the started matrix"
			+ " could modify the object passed. It should be considered and can be used for a feedback mechanism between matrices.")
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
