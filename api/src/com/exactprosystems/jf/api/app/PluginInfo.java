////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import sun.plugin2.main.server.Plugin;

import java.io.Serializable;
import java.util.*;

public class PluginInfo implements Serializable
{
    private static final long serialVersionUID = -1595364917643729823L;

    private Map<ControlKind, ControlInfo>   controlMap;
    private Map<LocatorFieldKind, String>   fieldMap;

    public PluginInfo(Map<LocatorFieldKind, String> fieldMap)
    {
        this.controlMap = new HashMap<>();
        this.fieldMap = fieldMap;
    }
    
    public Set<String> nodeByControlKind(ControlKind kind)
    {
        if (this.controlMap == null)
        {
            return null;
        }
        return this.controlMap.get(kind).getTypes();
    }

    public ControlKind controlKindByNode (String node)
    {
        if (this.controlMap == null)
        {
            return ControlKind.Any;
        }
        Optional<ControlKind> optional = this.controlMap.entrySet()
        		.stream()
        		.filter(e -> e.getValue().getTypes().stream().anyMatch(s -> s.equals(node)))
        		.map(Map.Entry::getKey)
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

    public ControlInfo add(ControlKind kind) {
        ControlInfo controlInfo = new ControlInfo();
        this.controlMap.put(kind, controlInfo);
        return controlInfo;
    }

    public boolean isSupported(ControlKind kind)
    {
        return controlMap.containsKey(kind);
    }

    public boolean isAllowed(ControlKind kind, OperationKind operation) {
        return controlMap.containsKey(kind) && !controlMap.get(kind).getExcludes().contains(operation);
    }

    public class ControlInfo
    {
        Set<String> types;
        Set<OperationKind> excludes;

        public ControlInfo() {
            this.types = new HashSet<>();
            this.excludes = new HashSet<>();
        }

        public Set<String> getTypes() {
            return types;
        }

        public Set<OperationKind> getExcludes() {
            return excludes;
        }

        public ControlInfo setTypes(String ... types)
        {
            this.types.addAll(Arrays.asList(types));
            return this;
        }

        public ControlInfo addExcludes(OperationKind ... operations)
        {
            this.excludes.addAll(Arrays.asList(operations));
            return this;
        }
    }
}
