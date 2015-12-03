////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.app.AppConnection;
import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.client.IClientFactory;

public interface IMatrix
{
	void 					setDefaultApp(String id);
	IApplicationFactory 	getDefaultApp();
	
	void 					setDefaultClient(String id);
	IClientFactory 			getDefaultClient();

	AppConnection			getDefaultApplicationConnection();
}
