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
	 */
	IWindow 			getFirstWindow();
	/**
	 * @param name searching window name
	 * @return IWindow or null, if window not found by passed name
	 */
	IWindow 			getWindow(String name);
	/**
	 * @return copy collection of windows from a dictionary
	 */
	Collection<IWindow> getWindows();
	boolean 			containsWindow(String dialogName);
}
