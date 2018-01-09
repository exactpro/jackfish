////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Collection;

public interface IGuiDictionary
{
	/**
	 * @return first IWindow from a dictionary or null, if list of windows is empty
	 *
	 * @see IWindow
	 */
	IWindow 			getFirstWindow();
	/**
	 * @param name searching window name
	 * @return IWindow or null, if window not found by passed name
	 *
	 * @see IWindow
	 */
	IWindow 			getWindow(String name);
	/**
	 * @return copy collection of windows from a dictionary
	 *
	 * @see IWindow
	 */
	Collection<IWindow> getWindows();
	/**
	 * Check, that a window with passed name is present in the dictionary
	 * @param dialogName a name, which used for found a window
	 * @return true, if in the dictionary window with passed name is present and false otherwise
	 */
	boolean 			containsWindow(String dialogName);
}
