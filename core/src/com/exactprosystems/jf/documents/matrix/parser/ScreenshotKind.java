////////////////////////////////////////////////////////////////////////////////
//Copyright (c) 2009-2015, Exactpro Systems, LLC
//Quality Assurance & Related Development for Innovative Trading Systems.
//All rights reserved.
//This is unpublished, licensed software, confidential and proprietary
//information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.exactprosystems.jf.api.common.Str;

public enum ScreenshotKind
{
    Never,
    OnStart,
    OnFinish,
    OnError,
    OnStartOrError,
    OnFinishOrError,
    ;
    
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
    
    public static List<String> names()
    {
        return Arrays.stream(values()).map(k -> k.name()).collect(Collectors.toList()) ;
    }
}
