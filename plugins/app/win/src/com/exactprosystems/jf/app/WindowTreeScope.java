/*******************************************************************************
 * Copyright (c) 2009-2018, Exactpro Systems LLC
 * www.exactpro.com
 * Build Software to Test Software
 *
 * All rights reserved.
 * This is unpublished, licensed software, confidential and proprietary
 * information which is the property of Exactpro Systems LLC or its licensors.
 ******************************************************************************/

package com.exactprosystems.jf.app;

public enum WindowTreeScope
{
    Element 	(1), // found only on element
    Children 	(2), // found only children of current element
    Descendants (4), // found on children of children of current component
    Subtree 	(7), // found from root element
    Parent 		(8), // not supported
    Ancestors 	(16);// not supported
    
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