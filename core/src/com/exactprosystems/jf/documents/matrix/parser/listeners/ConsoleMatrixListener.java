////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

public class ConsoleMatrixListener extends ConsoleErrorMatrixListener
{
	public ConsoleMatrixListener(boolean showShortPaths)
	{
		super();
		
		this.showShortPaths = showShortPaths;
	}
	
	@Override
	public void matrixStarted(Matrix matrix)
	{
		super.matrixStarted(matrix);
        System.out.println(String.format("Matrix '%s' started...", matrix.getName()));
	}

	@Override
	public void matrixFinished(Matrix matrix, int passed, int failed)
	{
		super.matrixFinished(matrix, passed, failed);
        System.out.println(String.format("Matrix '%s' finished.      PASSED: %d FAILED: %d", matrix.getName(), passed, failed));
	}

	@Override
	public void finished(Matrix matrix, MatrixItem action, Result result)
	{	
		super.finished(matrix, action, result);
        System.out.println(String.format("%s[%3d]  %-80s  %S", matrix.getName(), action.getNumber(), (this.showShortPaths ? action.getItemName() : action.getPath()), result));
	}
	
	private boolean showShortPaths = false;
	
}
