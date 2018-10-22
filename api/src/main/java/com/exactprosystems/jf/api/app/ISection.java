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

import com.exactprosystems.jf.api.app.IWindow.SectionKind;

import java.util.Collection;
import java.util.List;


public interface ISection
{
	/**
	 * Check, that other controls from the section has reference to the passed control
	 * @param control which used for searching references
	 * @return true, if any control (except the passed control) from the section has reference to the passed control. Otherwise return false
	 *
	 * @see IControl
	 */
	boolean hasReferences(IControl control);
	/**
	 * @return {@code copy} Collection of the controls
	 *
	 * @see IControl
	 */
	Collection<IControl> getControls();
	/**
	 * Add control to the section
	 * @param control which will added to the section
	 *
	 * @see IControl
	 */
	void addControl(IControl control);
	/**
	 * Insert the passed control to the passed index on the section
	 *
	 * @param index for inserting a control
	 * @param control which will inserting
	 *
	 * @see IControl
	 */
	void addControl(int index, IControl control);
	/**
	 * @return the first control from the section or {@code null}, if section is empty
	 *
	 * @see IControl
	 */
	IControl getFirstControl();
	/**
	 * Get the a from the section by passed control name ( control id)
	 * @param name a id of the searching control
	 * @return the control from the section or {@code null}, if control with passed id not found
	 */
	IControl getControlById(String name);
	/**
	 * Find the control by id {@code name + String.valueOf(obj)}
	 *
	 * @see ISection#getControlById(String)
	 */
	IControl getControlByIdAndValue(String name, Object obj);
	/**
	 * @return a list of names of all controls from the section
	 */
	List<String> getControlsNames();
	/**
	 * Set the window and the sectionKind to the section
	 *
	 * @see SectionKind
	 */
	void setSection(IWindow window, SectionKind kind);
	/**
	 * @return sectionKind, which will added via {@link ISection#setSection(IWindow, SectionKind)}
	 *
	 * @see SectionKind
	 */
	IWindow.SectionKind getSectionKind();
	/**
	 * @return a window, which will added via {@link ISection#setSection(IWindow, SectionKind)}
	 *
	 * @see IWindow
	 */
	IWindow getWindow();
}
