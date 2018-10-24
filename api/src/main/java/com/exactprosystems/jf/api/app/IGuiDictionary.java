/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

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
