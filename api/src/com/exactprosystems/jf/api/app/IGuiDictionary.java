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
	/**
	 * return window from dictionary
	 * @param name searching window name
	 * @return IWindow or null, if window by name not found
	 */
	IWindow 			getWindow(String name);
	Collection<IWindow> getWindows();
	boolean 			containsWindow(String dialogName);
}
