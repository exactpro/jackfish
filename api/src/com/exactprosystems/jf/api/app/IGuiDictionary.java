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
	/**
	 * @return first IWindow from a dictionary or null, if windows list are empty
	 */
	IWindow 			getFirstWindow();
	/**
	 * @param name searching window name
	 * @return IWindow or null, if window with not found by passed name
	 */
	IWindow 			getWindow(String name);
	/**
	 * @return copy collection of windows from a dictionary
	 */
	Collection<IWindow> getWindows();
	boolean 			containsWindow(String dialogName);
}
