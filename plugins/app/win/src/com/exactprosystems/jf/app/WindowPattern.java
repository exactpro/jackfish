////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

public enum WindowPattern
{
	InvokePattern(10000),
	SelectionPattern(10001),
	ValuePattern(10002),
	RangeValuePattern(10003),
	ScrollPattern(10004),
	ExpandCollapsePattern(10005),
	GridPattern(10006),
	GridItemPattern(10007), 
	MultipleViewPattern(10008),
	WindowPattern(10009),
	SelectionItemPattern(10010),
	DockPattern(10011), 
	TablePattern(10012),
	TableItemPattern(10013),
	TextPattern(10014),
	TogglePattern(10015),
	TransformPattern(10016),
	ScrollItemPattern(10017),
	
	ItemContainerPattern(10019),
	VirtualizedItemPattern(10020),
	SynchronizedInputPattern(10021),
	;
	
	private WindowPattern(int id)
	{
		this.id = id;
	}
	
    public int getId()
    {
    	return this.id;
    }

    public static WindowPattern byId(int id)
    {
    	for (WindowPattern item : values())
    	{
    		if (item.id == id)
    		{
    			return item;
    		}
    	}
    	
    	return null;
    }
    
	private int id;
}
