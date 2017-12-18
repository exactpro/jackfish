////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

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
