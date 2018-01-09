////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
