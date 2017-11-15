////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import java.util.Date;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;

public interface IMatrix
{
	void 					setDefaultApp(String id);
	IApplicationFactory 	getDefaultApp();
	
	void 					setDefaultClient(String id);
	IClientFactory 			getDefaultClient();

    MatrixConnection        start(Date time, Object parameter) throws Exception;
    void                    stop();
}
