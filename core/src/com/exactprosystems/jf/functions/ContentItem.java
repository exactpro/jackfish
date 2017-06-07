////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

public class ContentItem
{
    private String name;
    private int level;
    
    public ContentItem()
    {
        this("");
    }

    public ContentItem(String name)
    {
        this.name = name;
        this.level = 0;
    }
    
    @Override
    public String toString()
    {
        return this.name;
    }
}
