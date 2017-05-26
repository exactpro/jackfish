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
import com.exactprosystems.jf.api.error.common.RowExpiredException;
import com.exactprosystems.jf.api.error.common.WrongExpressionException;


public class RowTable implements Map<String, Object>, Cloneable
{
    private Map<Header, Object> currentRow;    
    private Table table;
    private int index = 0;
    
    public RowTable(Table table, int index)
    {
        this.table = table;
        this.index = index;
        this.currentRow = null;
    }
    
    public int index()
    {
        return this.index;
    }
    
    @Override
    public String toString()
    {
        checkRow();
        return this.currentRow.toString();
    }
    
    public CopyRowTable copy(Set<String> names)
    {
        checkRow();
        LinkedHashMap<String, Object> map = this.currentRow.entrySet()
                .stream()
                .filter(e -> names.contains(e.getKey().name))
                .collect(Collectors.toMap(e -> e.getKey().name, e -> e.getValue(), (k,v) -> k, LinkedHashMap::new));
        
        return new CopyRowTable(map);
    }
    
    public CopyRowTable copy()
    {
        checkRow();
        return new CopyRowTable(asLinkedMap());
    }
    
    public LinkedHashMap<String, Object> asLinkedMap()
    {
        checkRow();
        return asLinkedMap(this.currentRow);
    }
    
    public static LinkedHashMap<String, Object> asLinkedMap(Map<Header, Object> map)
    {
        return map.entrySet()
                .stream()
                .collect(Collectors.toMap(e -> e.getKey().name, e -> e.getValue(), (k,v) -> k, LinkedHashMap::new));
    }
    
    //==============================================================================================
    // Interface Cloneable
    //==============================================================================================
    @Override
    public RowTable clone() throws CloneNotSupportedException
    {
        RowTable clone = new RowTable(this.table, this.index); 
        clone.putAll(this);
        return clone;
    }


    //==============================================================================================
    // Interface Map
    //==============================================================================================

    @Override
    public int size()
    {
        checkRow();
        return this.currentRow.size();
    }

    @Override
    public boolean isEmpty()
    {
        checkRow();
        return this.currentRow.isEmpty();
    }

    @Override
    public boolean containsKey(Object key)
    {
        checkRow();
        return keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value)
    {
        checkRow();
        return this.currentRow.containsValue(value);
    }

    @Override
    public Object get(Object key)
    {
        checkRow(); // TODO edit here
        
//        System.err.println("@ " + this.currentRow.values());
        Header header = this.table.headerByName(Str.asString(key));
        Object value = this.currentRow.get(header);
        System.err.println(">> RowTable.get " + header + " " + value);
        value = this.table.convertCell(this.currentRow, header, value, null);
        if(value instanceof Exception)
        {
            Exception e = (Exception) value;
            throw new WrongExpressionException(e.getMessage());
        }
//        System.err.println("@@@ get " + key + " " + value);
        return value;
    }

    @Override
    public Object put(String key, Object value)
    {
        checkRow();
        return this.table.setValue(index, key, value);
    }

    @Override
    public Object remove(Object key)
    {
        checkRow();
        return put(key == null ? null : key.toString(), null);
    }

    @Override
    public void putAll(Map<? extends String, ? extends Object> m)
    {
        checkRow();
        for (Map.Entry<? extends String, ? extends Object> entry : m.entrySet())
        {
            put(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void clear()
    {
        checkRow();
        for (Map.Entry<Header, Object> entry : this.currentRow.entrySet())
        {
            entry.setValue(null);
        }
    }

    @Override
    public Set<String> keySet()
    {
        checkRow();
        Set<String> res = new LinkedHashSet<>();
        this.currentRow.keySet().forEach(k -> res.add(k.name));
        return res;
    }

    @Override
    public Collection<Object> values()
    {
        checkRow();
        return this.currentRow.values();
    }

    @Override
    public Set<Map.Entry<String, Object>> entrySet()
    {
        checkRow();
        Map<String, Object> res = new LinkedHashMap<>();
        for (Map.Entry<Header, Object> entry : this.currentRow.entrySet())
        {
            res.put(entry.getKey().name, entry.getValue());
        }
        return res.entrySet();
    }

    private void checkRow() throws RowExpiredException
    {
        if (this.currentRow == null)
        {
            this.currentRow = this.table.getInner(this.index);
            return;
        }
        if (this.table.getInner(this.index) != this.currentRow)
        {
            throw new RowExpiredException("Expired");
        }
    }

}
