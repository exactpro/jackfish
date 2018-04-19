/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.tool.matrix;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

import org.apache.log4j.Logger;

public class MatrixListenerFx implements IMatrixListener
{
	public MatrixListenerFx()
	{
		this.ok = true;
	}

	public void reset(Matrix matrix)
	{
		this.ok = true;
		logger.trace("reset()");
	}

	public void matrixStarted(Matrix matrix)
	{
		logger.trace("matrixStarted()");
	}

	public void matrixFinished(Matrix matrix, int passed, int failed)
	{
		logger.trace("matrixFinished(...)");
	}

	public void started(Matrix matrix, MatrixItem item)
	{
		logger.trace(String.format("started(%s)", item.getPath()));
	}

	public void paused(Matrix matrix, MatrixItem item)
	{
		logger.trace(String.format("paused(%s)", item.getPath()));
	}

	public void finished(Matrix matrix, MatrixItem item, Result result)
	{
		logger.trace(String.format("finished(%s, %s)", item.getPath(), result));
	}

	public void error(Matrix matrix, int lineNumber, MatrixItem item, String message)
	{
		this.exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
		logger.error(exceptionMessage);
		this.ok = false;
	}

	public String getExceptionMessage()
	{
		return this.exceptionMessage;
	}

	public boolean isOk()
	{
		return this.ok;
	}

	private boolean				ok		= false;
	private String				exceptionMessage;


	private static final Logger	logger	= Logger.getLogger(MatrixListenerFx.class);
}
