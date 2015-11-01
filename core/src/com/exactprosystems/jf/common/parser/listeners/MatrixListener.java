////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.parser.listeners;

import com.exactprosystems.jf.common.parser.Matrix;
import com.exactprosystems.jf.common.parser.Result;
import com.exactprosystems.jf.common.parser.items.MatrixItem;
import org.apache.log4j.Logger;

public class MatrixListener implements IMatrixListener
{
	public MatrixListener()
	{
		this.ok = true;		
	}

	public void matrixStarted(Matrix matrix)
	{
		this.ok = true;
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
		exceptionMessage = String.format("error(%d, %s, %s)", lineNumber, item == null ? "<null>" : item.getPath(), message);
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
	
	private boolean ok = false;
	private String exceptionMessage;

	private static final Logger logger = Logger.getLogger(MatrixListener.class);
}
