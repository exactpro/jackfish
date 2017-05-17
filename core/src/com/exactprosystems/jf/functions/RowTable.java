////////////////////////////////////////////////////////////////////////////////
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.functions;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.exactprosystems.jf.api.common.Str;
import com.exactprosystems.jf.functions.Header.HeaderType;


public class RowTable implements Map<String, Object>, Cloneable
{
    public RowTable(Map<Header, Object> map)
    {
        if (map == null)
        {
            throw new NullPointerException("map");
        }
        
        this.source = map;
    }

    public RowTable()
    {
        this(new LinkedHashMap<Header, Object>());
    }
    
    @Override
    public String toString()
    {
        return this.source.toString();
    }
    
    public CopyRowTable copy()
    {
        return new CopyRowTable(this.source);
    }
    
    public LinkedHashMap<String, Object> asLinkedMap()
    {
        return this.source.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().name, e -> e.getValue(), (k,v) -> k, LinkedHashMap::new));
    }
    
    //==============================================================================================
    // Interface Cloneable
    //==============================================================================================
    @Override
    public RowTable clone() throws CloneNotSupportedException
    {
        RowTable clone = new RowTable(); 
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
        return keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        return this.source.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        for (Map.Entry<Header, Object> entry : this.source.entrySet())
        {
            if (entry.getKey().name.equals(key))
            {
                if(entry.getValue() instanceof Exception)
                {
                    Exception e = (Exception) entry.getValue();
                    throw new RuntimeException(e.getMessage(), e);
                }
                else
                {
                    return entry.getValue();
                }
            }
        }
        
        return null;
    }

    @Override
    public Object put(String key, Object value)
    {
        for (Map.Entry<Header, Object> entry : this.source.entrySet())
        {
            if (entry.getKey().name.equals(key))
            {
                Object res = entry.getValue();
                entry.setValue(value);
                return res;
            }
        }

        Header header = new Header(key, HeaderType.STRING);
        this.source.put(header, value);
        
        return null;
    }

    @Override
    public Object remove(Object key)
    {
        return put(key == null ? null : key.toString(), null);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        for (Map.Entry<Header, Object> entry : this.source.entrySet())
        {
            entry.setValue(null);
        }
    }

    @Override
    public Set<String> keySet()
    {
        Set<String> res = new LinkedHashSet<>();
        for (Header key : this.source.keySet())
        {
            res.add(key.name);
        }
        return res;
    }

    @Override
    public Collection<Object> values()
    {
        return this.source.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
        Map<String, Object> res = new LinkedHashMap<>();
        for (Map.Entry<Header, Object> entry : this.source.entrySet())
        {
            res.put(entry.getKey().name, entry.getValue());
        }
        return res.entrySet();
    }

    private Map<Header, Object> source;
}
