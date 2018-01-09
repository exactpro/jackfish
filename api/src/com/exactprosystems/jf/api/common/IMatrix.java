////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;

import java.util.Date;

public interface IMatrix
{
	/**
	 * Set a application to default for the matrix.
	 * If the app with passed id not found in the configuration, nothing will happen
	 *
	 * @param id the id of application, which will stored in the matrix
	 */
	void setDefaultApp(String id);
	/**
	 * @return a application, which was set via {@link IMatrix#setDefaultApp(String)} or null
	 */
	IApplicationFactory getDefaultApp();

	/**
	 * Set a client to default for the matrix.
	 * If the client with passed id not found in the configuration, nothing will happen
	 *
	 * @param id the id of client, which will stored in the matrix
	 */
	void setDefaultClient(String id);
	/**
	 * @return a client factory, which was set via {@link IMatrix#setDefaultClient(String)} or null
	 */
	IClientFactory getDefaultClient();

	/**
	 * Start the matrix at the passed name and with passed parameter.
	 * If the matrix is a library, nothing will happen
	 *
	 * @param time the time, when the matrix will started. If the time is null, will used new {@link Date}
	 * @param parameter a parameter for the matrix. For use this parameter on the matrix,
	 *
	 * @return a MatrixConnection object for observing the matrix
	 *
	 * @throws Exception if the matrix has errors or can't create report for the matrix (e.g. no free space on hard disk)
	 * @see MatrixConnection
	 */
	MatrixConnection start(Date time, Object parameter) throws Exception;
	/**
	 * Stop the matrix. If the matrix was not running, nothing will happen
	 */
	void stop();
}
