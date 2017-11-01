////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import com.exactprosystems.jf.api.common.IFactory;

import java.util.Set;

public interface IApplicationFactory  extends IFactory
{
    void                init(IGuiDictionary dictionary);

	Set<ControlKind> 	supportedControlKinds();

	IApplication 		createApplication() throws Exception;
	String 				getRemoteClassName();
	
	IGuiDictionary 		getDictionary();

	boolean				isAllowed(ControlKind kind, OperationKind operation);
	boolean				isSupported(ControlKind kind);

    PluginInfo          getInfo();
}
