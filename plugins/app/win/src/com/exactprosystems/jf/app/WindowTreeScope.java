////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

public enum WindowTreeScope
{
    Element 	(1),
    Children 	(2),
    Descendants (4),
    Subtree 	(7),
    Parent 		(8),
    Ancestors 	(16);
    
    WindowTreeScope(int value)
    {
    	this.value = value;
    }
    
    public int getValue()
    {
    	return this.value;
    }
    
    private int value;
}