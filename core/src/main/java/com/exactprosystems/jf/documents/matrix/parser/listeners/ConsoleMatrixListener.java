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

public class ConsoleMatrixListener extends ConsoleErrorMatrixListener
{
	private final boolean showShortPaths;

	public ConsoleMatrixListener(boolean showShortPaths)
	{
		super();
		this.showShortPaths = showShortPaths;
	}

	@Override
	public void matrixStarted(Matrix matrix)
	{
		super.matrixStarted(matrix);
		System.out.println(String.format("Matrix '%s' started...", matrix.getNameProperty()));
	}

	@Override
	public void matrixFinished(Matrix matrix, int passed, int failed)
	{
		super.matrixFinished(matrix, passed, failed);
		System.out.println(String.format("Matrix '%s' finished.      PASSED: %d FAILED: %d", matrix.getNameProperty(), passed, failed));
	}

	@Override
	public void finished(Matrix matrix, MatrixItem action, Result result)
	{
		super.finished(matrix, action, result);
		System.out.println(String.format("%s[%3d]  %-80s  %S", matrix.getNameProperty(), action.getNumber(), (this.showShortPaths ? action.getItemName() : action.getPath()), result));
	}
}
