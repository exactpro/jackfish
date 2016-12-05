////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import com.exactprosystems.jf.api.common.Str;

public enum ScreenshotKind
{
    Never,
    OnStart,
    OnFinish,
    OnError;
    
    public static ScreenshotKind valueByName(String name) throws Exception
    {
        if (Str.IsNullOrEmpty(name))
        {
            return Never;
        }

        for (ScreenshotKind a : values())
        {
            if(a.name().equals(name))
            {
                return a;
            }
        }
        throw new Exception("Unknown name: " + name);
    }
}
