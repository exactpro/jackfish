////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;


public class PluginInfo
{
    public PluginInfo()
    {
    }
    
    public String[] nodeByControlKind(ControlKind kind)
    {
        return new String[] {};
    }
    
    public ControlKind controlKindByString(String name)
    {
        return ControlKind.Any;
    }
    
    public String nodeByLocatorKind (LocatorFieldKind kind)
    {
        return "";
    }
    
}
