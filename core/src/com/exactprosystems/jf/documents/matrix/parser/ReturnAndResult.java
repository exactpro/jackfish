/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.error.ErrorKind;
import com.exactprosystems.jf.common.evaluator.AbstractEvaluator;
import com.exactprosystems.jf.common.report.ReportBuilder;
import com.exactprosystems.jf.documents.config.Context;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixError;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;
import com.exactprosystems.jf.documents.matrix.parser.listeners.IMatrixListener;

/**
 * This class used for return result of executing a Matrix item
 *
 * @see MatrixItem#execute(Context, IMatrixListener, AbstractEvaluator, ReportBuilder)
 */
public class ReturnAndResult
{
	private Result      result;
	private Object      out;
	private MatrixError error;
	private long        time;

	public ReturnAndResult(long start, ReturnAndResult other, Result result)
	{
		this.time = System.currentTimeMillis() - start;
		this.result = result;
		this.out = other.out;
		this.error = other.error;
	}

	public ReturnAndResult(long start, ReturnAndResult other)
	{
		this.time = System.currentTimeMillis() - start;
		this.result = other.result;
		this.out = other.out;
		this.error = other.error;
	}

	public ReturnAndResult(long start, MatrixError error, Result result)
	{
		this.time = System.currentTimeMillis() - start;
		this.result = result;
		this.out = null;
		this.error = error;
	}

	public ReturnAndResult(long start, Result result, String message, ErrorKind kind, MatrixItem place)
	{
		this.time = System.currentTimeMillis() - start;
		this.result = result;
		this.out = null;
		this.error = message == null ? null : new MatrixError(message, kind, place);
	}

	public ReturnAndResult(long start, Result result, Object out)
	{
		this.time = System.currentTimeMillis() - start;
		this.result = result;
		this.out = out;
	}

	public ReturnAndResult(long start, Result result)
	{
		this(start, result, null);
	}

	public Result getResult()
	{
		return this.result;
	}

	public Object getOut()
	{
		return this.out;
	}

	public MatrixError getError()
	{
		return this.error;
	}

	public long getTime()
	{
		return this.time;
	}
}
