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

package com.exactprosystems.jf.documents.matrix.parser.listeners;

import com.exactprosystems.jf.documents.matrix.Matrix;
import com.exactprosystems.jf.documents.matrix.parser.Result;
import com.exactprosystems.jf.documents.matrix.parser.items.MatrixItem;

/**
 * The interface for matrix listener
 */
public interface IMatrixListener extends Cloneable
{
	/**
	 * Reset the listener. It's mean, that methods {@link IMatrixListener#isOk()} after call this method will return true
	 */
	void reset(Matrix matrix);
	/**
	 * Notify, that the passed matrix started
	 */
	void matrixStarted(Matrix matrix);
	/**
	 * Notify, that passed matrix finished. Parameters passed and failed indicate count of passed and failed children of MatrixRoot ( usually it is TestCase)
	 *
	 * @param matrix the finished matrix
	 * @param passed count of passed TestCase
	 * @param failed count of failed TestCase
	 */
	void matrixFinished(Matrix matrix, int passed, int failed);
	/**
	 * Notify, that the item from the matrix start executing
	 *
	 * @param matrix the matrix
	 * @param item the item, which is started
	 */
	void started(Matrix matrix, MatrixItem item);
	/**
	 * Notify, that the item from the matrix is paused ( usually on breakpoint)
	 *
	 * @param matrix the matrix
	 * @param item the paused item
	 */
	void paused(Matrix matrix, MatrixItem item);
	/**
	 * Notify, that the item from the matrix is finished
	 *
	 * @param matrix the matrix
	 * @param item the finished item
	 * @param result the result of executing the item
	 */
	void finished(Matrix matrix, MatrixItem item, Result result);
	/**
	 * Notify error on the matrix
	 *
	 * @param matrix the matrix
	 * @param lineNumber the line number of error the item
	 * @param item the error item. It may be {@code null}
	 * @param message the message of error
	 */
	void error(Matrix matrix, int lineNumber, MatrixItem item, String message);
	/**
	 * @return the last exception
	 */
	String getExceptionMessage();
	/**
	 * @return true, if matrix has no errors
	 */
	boolean isOk();
}
