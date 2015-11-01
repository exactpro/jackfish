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

public class OperationResult implements Serializable, Map<String, String>
{

	private static final long serialVersionUID = -2015741070415094348L;
	private boolean ok = false;
	private boolean permittedOperation = false;
	private String text = null;
	private LinkedHashMap<String, String> map = new LinkedHashMap<>();
	private Map<String, ValueAndColor> colorMap = new LinkedHashMap<>();
	private String[][] array;

	private boolean mapFilled = false;
	private boolean colorMapFilled = false;
	private boolean arrayFilled = false;

	public void setText(String text)
	{
		this.text = text;
	}

	public void setOk(boolean ok)
	{
		this.ok = ok;
	}

	public void setMap(Map<String, String> map)
	{
		this.map.putAll(map);
		mapFilled = true;
	}

	public void setPermittedOperation(boolean flag)
	{
		this.permittedOperation = flag;
	}

	public boolean isPermittedOperation()
	{
		return permittedOperation;
	}

	public void setColorMap(Map<String, ValueAndColor> colorMap)
	{
		this.colorMap = colorMap;
		this.colorMapFilled = true;
	}

	public void setList(List<String> list)
	{
		for (int i = 0; i < list.size(); i++)
		{
			String s = list.get(i);
			this.map.put(String.valueOf(i), s);
		}
		this.mapFilled = true;
	}
	
	public String getText()
	{
		return this.text;
	}
	
	public boolean isOk()
	{
		return this.ok;
	}

	public Map<String,String> getMap()
	{
		return this.map;
	}

	public Map<String, ValueAndColor> getColorMap()
	{
		return colorMap;
	}

	public Collection<String> getList()
	{
		return this.map.values();
	}

	public void setArray(String[][] a)
	{
		this.array = a;
		this.arrayFilled = true;
	}

	public String[][] getArray()
	{
		return array;
	}

	@Override
	public String toString()
	{
		return "OpRes[ok=" + this.ok + ", text="  + this.text + "]";
	}

	@Override
	public int size()
	{
		return this.map.size();
	}

	@Override
	public boolean isEmpty()
	{
		return this.map.isEmpty();
	}

	@Override
	public boolean containsKey(Object key)
	{
		return this.map.containsKey(key);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.map.containsValue(value);
	}

	@Override
	public String get(Object key)
	{
		return this.map.get(key);
	}

	@Override
	public String put(String key, String value)
	{
		this.mapFilled = true;
		return this.map.put(key, value);
	}

	@Override
	public String remove(Object key)
	{
		return this.map.remove(key);
	}

	@Override
	public void putAll(Map<? extends String, ? extends String> m)
	{
		this.mapFilled = true;
		this.map.putAll(m);
	}

	@Override
	public void clear()
	{
		this.map.clear();
	}

	@Override
	public Set<String> keySet()
	{
		return this.map.keySet();
	}

	@Override
	public Collection<String> values()
	{
		return this.map.values();
	}

	@Override
	public Set<Entry<String, String>> entrySet()
	{
		return this.map.entrySet();
	}

	public boolean isMapFilled()
	{
		return mapFilled;
	}

	public boolean isColorMapIsFilled()
	{
		return colorMapFilled;
	}

	public boolean isArrayFilled()
	{
		return arrayFilled;
	}
}
