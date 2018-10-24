/*******************************************************************************
 * Copyright 2009-2018 Exactpro (Exactpro Systems Limited)
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/

package com.exactprosystems.jf.api.app;

import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

public abstract class PluginInfo implements Serializable
{
    private static final long serialVersionUID = -1595364917643729823L;

	public static final String ANY_TYPE = "*";

	private Map<ControlKind, ControlInfo>   controlMap;
    private Map<LocatorFieldKind, String>   fieldMap;
    private List<String> notStableList;

    public PluginInfo(Map<LocatorFieldKind, String> fieldMap, List<String> notStableList)
    {
        this.controlMap = new EnumMap<>(ControlKind.class);
        this.fieldMap = fieldMap;
		this.notStableList = notStableList;
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

    public ControlKind controlKindByNode(Node node)
    {
        if (this.controlMap == null)
        {
            return ControlKind.Any;
        }
		String nodeName = node.getNodeName();
		List<ControlKind> list = this.controlMap.entrySet()
        		.stream()
        		.filter(e -> e.getValue().getTypes().stream().anyMatch(s -> s.equals(nodeName)))
        		.map(Map.Entry::getKey)
				.collect(Collectors.toList());
		if (list.size() == 1)
		{
			return list.get(0);
		}
		return derivedControlKindByNode(node);
	}

	public ControlKind controlKindByType(String type)
	{
		if (this.controlMap == null)
		{
			return ControlKind.Any;
		}
		return this.controlMap.entrySet()
				.stream()
				.filter(e -> e.getValue().getTypes().stream().anyMatch(s -> s.equals(type)))
				.findFirst()
				.map(Map.Entry::getKey)
				.orElse(ControlKind.Any);
	}

	protected abstract ControlKind derivedControlKindByNode(Node node);

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
        ControlInfo controlInfo = this.controlMap.computeIfAbsent(kind, k -> new ControlInfo());
        controlInfo.addTypes(types);
    }

    public void addExcludes(ControlKind kind, OperationKind ... operations)
    {
        ControlInfo controlInfo = this.controlMap.computeIfAbsent(kind, k -> new ControlInfo());
        controlInfo.addExcludes(operations);
    }
    
    public Set<ControlKind> supportedControlKinds()
    {
        return this.controlMap.keySet().stream()
                .sorted((c1,c2) -> c1.name().compareTo(c2.name()))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

	public boolean isStable(String text)
	{
		return !this.notStableList.contains(text);
	}

	public boolean isSupported(ControlKind kind)
    {
        return controlMap.containsKey(kind);
    }

    public boolean isAllowed(ControlKind kind, OperationKind operation) 
    {
        return controlMap.containsKey(kind) && !controlMap.get(kind).getExcludes().contains(operation);
    }

    private static class ControlInfo implements Serializable
    {
        private static final long serialVersionUID = -6381695821017225511L;

        private Set<String> types;
        private Set<OperationKind> excludes;

        private ControlInfo()
        {
            this.types = new HashSet<>();
            this.excludes = new HashSet<>();
        }

        private Set<String> getTypes()
        {
            return types;
        }

        private Set<OperationKind> getExcludes() {
            return excludes;
        }

        private void addTypes(String ... types)
        {
            this.types.addAll(Arrays.asList(types));
        }

        private void addExcludes(OperationKind ... operations)
        {
            this.excludes.addAll(Arrays.asList(operations));
        }
    }
}
