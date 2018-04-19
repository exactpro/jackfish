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

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;


public interface IWindow
{
	enum SectionKind
	{ 
		Any,
		Self,
		OnOpen, 
		Run, 
		OnClose,
		Close,
	}

	void				setName(String name);
	String 				getName();

	/**
	 * Get section by passed SectionKind.
	 * @param kind a kind of section
	 * @return section from the window
	 *
	 * @see ISection
	 * @see SectionKind
	 */
	ISection 			getSection(SectionKind kind);

	/**
	 * Check, that any control from the window has reference to the passed control
	 * @param control which used for found reference.
	 * @return true, if any controls from the window has reference to the passed control. If passed control is {@code null} or has empty id, will return false
	 *
	 * @see IControl
	 */
	boolean 			hasReferences(IControl control);
	/**
	 * Remove the passed control from the window.
	 * @param control the control, which will removed
	 */
	void 				removeControl(IControl control);
	/**
	 * Add the passed control to a section
	 * @param kind the kind of section, which will used for inserting the passed control
	 * @param control the control, which will inserting
	 */
	void 				addControl(SectionKind kind, IControl control);
	/**
	 * Return copy of controls from the a section.
	 * @param kind the kind of section, which from need return the controls. If kind is {@code null}, will return controls from all sections from the window
	 * @return a collection, which contains controls.
	 */
	Collection<IControl> getControls(SectionKind kind);
	/**
	 * Get the first control from a section
	 * @param kind a kind of section. If kind is {@code null}, will get the first control from section with kind {@link SectionKind#Run}
	 * @return the first control from a section. If section has no controls, will return {@code null}
	 */
	IControl 			getFirstControl(SectionKind kind);
	/**
	 * Get a control with passed id from a section
	 * @param kind the kind of section. If kind is {@code null}, the controls will found in the all sections
	 * @param name the id of control for found
	 * @return the control by passed name. If control not found, will return {@code null}
	 */
	IControl 			getControlForName(SectionKind kind, String name);
	/**
	 * @return Return the link to owner control for the passed control. If the passed control has no owner, will return {@code null}
	 *
	 * @see IControl#getOwnerID()
	 */
	IControl 			getOwnerControl(IControl control);
	/**
	 * @return if the passed control has referenceId, will return the control with it id. if control with refId not found, will return {@code null}.
	 * Otherwise will return {@code null}
	 *
	 * @see IControl#getRefID()
	 */
	IControl 			getReferenceControl(IControl control);
	/**
	 * @return if the passed control has rowsId, will return the control with it id. if control with rowsId not found, will return {@code null}.
	 * Otherwise will return {@code null}
	 *
	 * @see IControl#getRowsId()
	 */
	IControl			getRowsControl(IControl control);
	/**
	 * @return if the passed control has headerId, will return the control with it id. if control with headerId not found, will return {@code null}.
	 * Otherwise will return {@code null}
	 *
	 * @see IControl#getHeaderId()
	 */
	IControl			getHeaderControl(IControl control);
	/**
	 * @return a first control from section {@link SectionKind#Self}. If self section is empty, will return {@code null}
	 *
	 * @see SectionKind
	 */
	IControl 			getSelfControl();
	/**
	 * Check, that ids from the passed collection are contains in the window.
	 * @param set a collection with id
	 * @throws Exception if id from the passed collections not found in all sections
	 */
	void 				checkParams(Collection<String> set) throws Exception;
	/**
	 * @return true, if the window contains the control with passed id in all sections. Otherwise will return false
	 */
	boolean 			containsControl(String controlName);
	/**
	 * @return list of controls, which matched by passed predicate
	 */
    List<IControl>      allMatched(BiFunction<ISection, IControl, Boolean> predicate);
}
