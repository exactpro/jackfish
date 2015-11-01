////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.common;

import com.exactprosystems.jf.api.app.IApplicationFactory;
import com.exactprosystems.jf.api.app.IApplicationPool;
import com.exactprosystems.jf.api.client.IClientFactory;
import com.exactprosystems.jf.api.client.IClientsPool;
import com.exactprosystems.jf.api.service.IServicesPool;

import java.io.Reader;
import java.util.Date;

public interface IContext
{
	IMatrixRunner createRunner(Reader reader, Date startTime, Object parameter) throws Exception;

	IClientsPool 		getClients();
	IServicesPool 		getServices();
	IApplicationPool 	getApplications();
	IClientFactory 		getDefaultClient();
	IApplicationFactory getDefaultApp();
}
