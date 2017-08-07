////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Collection;

public interface IGuiDictionary
{
	IWindow 			getFirstWindow();
	IWindow 			getWindow(String name)  throws Exception;
	Collection<IWindow> getWindows();
	boolean 			containsWindow(String dialogName);
}
