////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Map;

public interface IApplication
{
	final static String serviceName = "MyAppServiceName";
	
	void 				init(IApplicationPool pool, IApplicationFactory factory) throws Exception;

	boolean				connect(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	boolean 			start(int port, String jar, String work, String remoteClassName, Map<String, String> driverParameters, Map<String, String> parameters) throws Exception;
	void 				stop() throws Exception;

	IRemoteApplication 	service();
	IApplicationPool	getPool();
	IApplicationFactory	getFactory();
}
