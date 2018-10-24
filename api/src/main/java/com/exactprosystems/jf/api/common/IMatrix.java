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
