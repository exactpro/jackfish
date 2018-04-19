/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
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
