/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

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
