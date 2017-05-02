////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.error.common;

import com.exactprosystems.jf.api.common.IMatrixItem;
import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.api.error.JFException;

public class MatrixException extends JFException
{
	private static final long serialVersionUID = -8878908206137039974L;

	public MatrixException(int lineNumber, IMatrixItem item, Exception ex)
	{
		super("Matrix error in #" + lineNumber + (item == null ? " " : " '" + item.getItemName() + "' ") + ex.getMessage());
	}
	
	public MatrixException(int lineNumber, IMatrixItem item, String msg)
	{
		super("Matrix error in #" + lineNumber + (item == null ? " " : " '" + item.getItemName() + "' ") + msg);
	}


	@Override
	public ErrorKind getErrorKind()
	{
		return ErrorKind.MATRIX_ERROR;
	}
}
