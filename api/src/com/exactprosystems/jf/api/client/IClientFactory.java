////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.client;

import java.util.Set;

import com.exactprosystems.jf.api.common.IFactory;
import com.exactprosystems.jf.api.common.VersionSupported;

public interface IClientFactory  extends VersionSupported, IFactory
{
	void				init(IMessageDictionary dictionary);
	
	@Deprecated
	String[] 			wellKnownStartArgs();
	@Deprecated
	String[]			wellKnownConnectArgs();

	IClient 			createClient();
	Set<Possibility> 	possebilities();
	
	IMessageDictionary	getDictionary();
}
