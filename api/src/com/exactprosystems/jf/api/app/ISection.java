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
	boolean 				hasReferences(IControl control);
	Collection<IControl> 	getControls();
	void 					addControl(IControl control);
	void 					addControl(int index, IControl control);
	IControl 				getFirstControl();
	IControl 				getControlById(String name);
	IControl 				getControlByIdAndValue(String name, Object obj);
	List<String> 			getControlsNames();
	void 					setSection(IWindow window, SectionKind kind);
	IWindow.SectionKind 	getSectionKind();
    IWindow                 getWindow();
}
