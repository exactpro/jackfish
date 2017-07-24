////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
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
	void 					addControl(int index, IControl control) throws Exception;
	IControl 				getFirstControl();
	IControl 				getControlById(String name);
	IControl 				getControlByIdAndValue(String name, Object obj);
	List<String> 			getControlsNames();
	void 					setSection(IWindow window, SectionKind kind);
	IWindow.SectionKind 	getSectionKind();
    IWindow                 getWindow();
}
