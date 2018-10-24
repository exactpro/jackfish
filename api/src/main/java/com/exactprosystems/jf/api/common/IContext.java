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

package com.exactprosystems.jf.api.common;

import java.io.PrintStream;
import java.io.Reader;

public interface IContext
{
	/**
	 * Redirect printStream
	 * @param console new PrintStream
	 * @return this instance
	 */
	IContext setOut(PrintStream console);

	/**
	 * Create copy of the current instance of {@link IContext}
	 * @return copy of the current instance
	 */
	IContext createCopy();

	/**
	 * Load the matrix by passed name and reader, after it start the matrix with passed parameter <br>
	 * Return {@link MatrixConnection} object for observation the matrix
	 * @param string name of a matrix
	 * @param reader from which will load a matrix
	 * @param parameter started parameter for a matrix
	 * @return a {@link MatrixConnection} object for observation the matrix
	 * @throws Exception if something went wrong
	 *
	 * @see MatrixConnection
	 */
	MatrixConnection startMatrix(String string, Reader reader, Object parameter) throws Exception;
}
