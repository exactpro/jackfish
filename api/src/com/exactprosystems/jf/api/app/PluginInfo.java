////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

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
        if(this.controlMap.get(kind) == null)
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

    public void addTypes(ControlKind kind, String ... types)
    {
        ControlInfo controlInfo = this.controlMap.get(kind);
        if (controlInfo == null)
        {
            controlInfo = new ControlInfo();
            this.controlMap.put(kind, controlInfo);
        }
        controlInfo.setTypes(types);
    }

    public void addExcludes(ControlKind kind, OperationKind ... operations)
    {
        ControlInfo controlInfo = this.controlMap.get(kind);
        if (controlInfo == null)
        {
            controlInfo = new ControlInfo();
            this.controlMap.put(kind, controlInfo);
        }

        controlInfo.addExcludes(operations);
    }
    
    
    public boolean isSupported(ControlKind kind)
    {
        return controlMap.containsKey(kind);
    }

    public boolean isAllowed(ControlKind kind, OperationKind operation) {
        return controlMap.containsKey(kind) && !controlMap.get(kind).getExcludes().contains(operation);
    }

    private static  class ControlInfo implements Serializable
    {
        private static final long serialVersionUID = -6381695821017225511L;

        public Set<String> types;
        public Set<OperationKind> excludes;

        public ControlInfo() 
        {
            this.types = new HashSet<>();
            this.excludes = new HashSet<>();
        }

        public Set<String> getTypes() 
        {
            return types;
        }

        public Set<OperationKind> getExcludes() {
            return excludes;
        }

        public void setTypes(String ... types)
        {
            this.types.addAll(Arrays.asList(types));
        }

        public void addExcludes(OperationKind ... operations)
        {
            this.excludes.addAll(Arrays.asList(operations));
        }
    }
}
