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
