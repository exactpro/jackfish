////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
