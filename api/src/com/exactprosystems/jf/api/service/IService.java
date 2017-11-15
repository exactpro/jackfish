////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.service;

import com.exactprosystems.jf.api.common.IContext;

import java.util.Map;

public interface IService
{
	void 		init(IServicesPool pool, IServiceFactory factory, Map<String, String> parameters) throws Exception;
	boolean 	start(IContext context, Map<String, Object> parameters) throws Exception;
	void 		stop() throws Exception;

	IServicesPool	getPool();
	IServiceFactory	getFactory();
}
