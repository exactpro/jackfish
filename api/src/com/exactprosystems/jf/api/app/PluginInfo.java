////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.Map;

public class PluginInfo
{
    public PluginInfo(Map<ControlKind, String[]> controlMap, Map<LocatorFieldKind, String> fieldMap)
    {
        this.controlMap = controlMap;
        this.fieldMap = fieldMap;
    }
    
    public String[] nodeByControlKind(ControlKind kind)
    {
        if (this.controlMap == null)
        {
            return null;
        }
        return this.controlMap.get(kind);
    }
    
    public String controlKindByString(LocatorFieldKind kind)
    {
        if (this.fieldMap == null)
        {
            return null;
        }
        return this.fieldMap.get(kind);
    }
    
    public String nodeByLocatorKind (LocatorFieldKind kind)
    {
        return "";
    }
    
    private Map<ControlKind, String[]>      controlMap;
    private Map<LocatorFieldKind, String>   fieldMap;
}
