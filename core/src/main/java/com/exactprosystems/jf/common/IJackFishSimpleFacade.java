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
package com.exactprosystems.jf.common;

import java.io.InputStream;

import com.exactprosystems.jf.documents.matrix.parser.Parameters;

public interface IJackFishSimpleFacade {

	/**
	 * Should be called once before other methods
	 */
	void init(InputStream config) throws Exception;
	
	/**
	 * @return app connection ID
	 */
	String start(Parameters params) throws Exception;

	/**
	 * @return app connection ID
	 */
	String connectTo(Parameters params) throws Exception;

	/**
	 * @param connection - app connection ID
	 * @param actionName - JF action name
	 * @param params     - parameters for action
	 * @return           - return value is action-specific
	 * @throws Exception
	 */
	Object doAction(String connection, String actionName, Parameters params) throws Exception;

	/**
	 * @param connection - app connection ID
	 * @param params
	 * @throws Exception
	 */
	void stop(String connection, Parameters params) throws Exception;

}
