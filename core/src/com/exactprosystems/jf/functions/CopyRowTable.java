////////////////////////////////////////////////////////////////////////////////
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.exactprosystems.jf.api.common.Str;


public class CopyRowTable implements Map<String, Object>, Cloneable
{
	public CopyRowTable(Map<Header, Object> map)
	{
	    this();
	    
		if (map == null)
		{
			throw new NullPointerException("map");
		}
		map.forEach((k,v) -> this.source.put(k.name, v));
	}

	public CopyRowTable()
	{
	    this.source = new LinkedHashMap<String, Object>();
	}
	
    public void makeStrValues(Set<String> names)
    {
        this.source = this.source.entrySet()
                .stream()
                .filter(e -> names.contains(e.getKey()))
                .collect(Collectors.toMap(k -> k.getKey(), v -> Str.asString(v.getValue()), (k, v) -> k, LinkedHashMap::new));
    }

    @Override
	public String toString()
	{
		return this.source.toString();
	}
	
	@Override
    public int hashCode()
    {
        return Objects.hashCode(this.source);
    }

    @Override
    public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        CopyRowTable other = (CopyRowTable) obj;
        return Objects.equals(this.source, other.source);
    }

    //==============================================================================================
	// Interface Cloneable
	//==============================================================================================
	@Override
	public CopyRowTable clone() throws CloneNotSupportedException
	{
		CopyRowTable clone = new CopyRowTable(); 
		clone.putAll(this);
		return clone;
	}


	//==============================================================================================
	// Interface Map
	//==============================================================================================

	@Override
	public int size()
	{
		return this.source.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.source.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.source.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.source.containsValue(value);
	}

	@Override
	public Object get(Object key)
	{
	    return this.source.get(key);
	}

	@Override
	public Object put(String key, Object value)
	{
	    return this.source.put(key, value);
	}

	@Override
	public Object remove(Object key)
	{
	    return this.source.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> m)
	{
		this.source.putAll(m);
	}

	@Override
	public void clear()
	{
	    this.source.clear();
	}

	@Override
	public Set<String> keySet()
	{
	    return this.source.keySet();
	}

	@Override
	public Collection<Object> values()
	{
		return this.source.values();
	}

	@Override
	public Set<Map.Entry<String, Object>> entrySet()
	{
	    return this.source.entrySet();
	}

    private Map<String, Object> source;
}
