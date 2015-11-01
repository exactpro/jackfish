////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.IPool;

import java.util.List;
import java.util.Map;

public interface IApplicationPool extends IPool
{
	List<String> appNames();
	
	String[] 			wellKnownStartArgs(String id) throws Exception;
	String[] 			wellKnownConnectArgs(String id) throws Exception;
	ControlKind[]		supportedControlKinds(String id) throws Exception;
	
	IApplicationFactory	loadApplicationFactory(String id) throws Exception;
	AppConnection 		connectToApplication(String id, Map<String, String> args) throws Exception;
	AppConnection 		startApplication(String id, Map<String, String> params) throws Exception;
	void 				stopApplication(AppConnection connection) throws Exception;
	void 				stopAllApplications() throws Exception;
}
