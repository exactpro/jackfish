////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

public class PluginInfo implements Serializable
{
    private static final long serialVersionUID = -1595364917643729823L;

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

    public ControlKind controlKindByNode (String node)
    {
        if (this.controlMap == null)
        {
            return ControlKind.Any;
        }
        Optional<ControlKind> optional = this.controlMap.entrySet()
        		.stream()
        		.filter(e -> Arrays.stream(e.getValue()).anyMatch(s -> s.equals(node)))
        		.map(m -> m.getKey())
        		.findFirst();
        return optional.orElse(ControlKind.Any);
    }
    

    public String attributeName(LocatorFieldKind kind)
    {
        if (this.fieldMap == null)
        {
            return null;
        }
        return this.fieldMap.get(kind);
    }
    
    private Map<ControlKind, String[]>      controlMap;
    private Map<LocatorFieldKind, String>   fieldMap;

    public boolean isSupported(ControlKind kind)
    {
        return controlMap.containsKey(kind);
    }
}
