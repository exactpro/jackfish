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

import java.sql.Blob;

public interface MatrixConnection
{
	/**
	 * Join to a executing thread of the matrix.
	 *
	 * @param time a time for joining. If time is 0, it mean, that join until the matrix not executed fully
	 *
	 * @return true, if the matrix is executing fully after the passed time. Otherwise will return false
	 */
	boolean join(long time) throws Exception;

	/**
	 * Stop the matrix
	 */
	void stop();

	/**
	 * @return a Blob object, which represent a report of the matrix. This object used for stored the report in data base.
	 * If the matrix is library, will returned null
	 */
	Blob reportAsBlob() throws Exception;

	/**
	 * @return a count of passed children of the matrix root ( usually it is TestCase)
	 */
	int passed();

	/**
	 * @return * @return a count of failed children of the matrix root ( usually it is TestCase)
	 */
	int failed();

	/**
	 * @return true, if the matrix is running now. If the matrix is library, will return false.
	 */
	boolean isRunning();

	String getMatrixName();

	String getReportName();

	String getImagesDirPath();

	/**
	 * Stop the matrix.
	 *
	 * @see IMatrix#stop()
	 */
	void close() throws Exception;

}
