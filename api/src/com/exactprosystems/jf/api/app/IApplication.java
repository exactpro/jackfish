////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.SerializablePair;

import java.util.Map;

public interface IApplication
{
	final static String serviceName = "MyAppServiceName";
	
	void 				init(IApplicationPool pool, IApplicationFactory factory) throws Exception;

    int                 reconnect(Map<String, String> parameters) throws Exception;
    /**
	 * key   : pid <br>
	 * value : port
     */
	SerializablePair<Integer, Integer> connect(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	/**
	 * key   : pid <br>
	 * value : port
	 */
	SerializablePair<Integer, Integer> start(int startPort, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	void 				stop(boolean needKill) throws Exception;

	IRemoteApplication 	service();
	IApplicationPool	getPool();
	IApplicationFactory	getFactory();
}
