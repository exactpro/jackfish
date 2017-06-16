package com.exactprosystems.jf.api.app;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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

    public ControlInfo addTypes(String ... types)
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
