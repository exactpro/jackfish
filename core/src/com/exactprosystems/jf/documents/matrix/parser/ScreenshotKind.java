////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

public enum ScreenshotKind
{
    Never,
    OnStart,
    OnFinish,
    OnError;
    
    public static ScreenshotKind valueByName(String name)
    {
        for (ScreenshotKind a : values())
        {
            if(a.name().equals(name))
            {
                return a;
            }
        }
        
        return Never;
    }
}
