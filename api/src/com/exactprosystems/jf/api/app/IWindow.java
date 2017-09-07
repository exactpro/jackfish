////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Collection;
import java.util.List;
import java.util.function.BiFunction;


public interface IWindow
{
	public enum SectionKind 
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

	ISection 			getSection(SectionKind kind);

	boolean 			hasReferences(IControl control);
	void 				removeControl(IControl control);
	void 				addControl(SectionKind kind, IControl control) throws Exception;
	Collection<IControl> getControls(SectionKind kind);
	IControl 			getFirstControl(SectionKind kind) throws Exception;
	IControl 			getControlForName(SectionKind kind, String name);
	IControl 			getOwnerControl(IControl control) throws Exception;
	IControl 			getReferenceControl(IControl control);
	IControl			getRowsControl(IControl control) throws Exception;
	IControl			getHeaderControl(IControl control) throws Exception;
	IControl 			getSelfControl();

	void 				checkParams(Collection<String> set) throws Exception;
	boolean 			containsControl(String controlName) throws Exception;
    List<IControl>      allMatched(BiFunction<ISection, IControl, Boolean> predicat);

	
}
