////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.app;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

@Deprecated
public class UIProxy
{
	public UIProxy(long[] id)
	{
		this.id = id;
	}
	
	public UIProxy(List<Number> id)
	{
		this.id = new long[id.size()];
		for (int i = 0; i < id.size(); i++)
		{
			this.id[i] = id.get(i).longValue();
		}
	}

	public UIProxy(Number[] id)
	{
		this.id = new long[id.length];
		for (int i = 0; i < id.length; i++)
		{
			this.id[i] = id[i].longValue();
		}
	}
	
	public int getX()
	{
		return this.x;
	}

	public int getY()
	{
		return this.y;
	}

	public int getWidth()
	{
		return this.width;
	}

	public int getHeight()
	{
		return this.height;
	}

	public boolean isEmpty()
	{
		return this.id == null || this.id.length == 0;
	}
	
	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(UIProxy.class.getSimpleName()).append("{").append(Arrays.toString(this.id)).append("}");
		if (this.data != null)
		{
			for (Entry<KindInformation, DataTable> d : this.data.entrySet())
			{
				sb.append(d.getKey()).append("=").append(d.getValue()).append('\n');
			}
		}
		
		return sb.toString();
	}
	
	public long[] getId()
	{
		return this.id;
	}

	public DataTable getData(KindInformation kind)
	{
		if (this.data == null)
		{
			return null;
		}
		return this.data.get(kind);
	}

	public void setData(KindInformation kind, DataTable data)
	{
		if (this.data == null)
		{
			this.data = new HashMap<KindInformation, DataTable>();
		}
		this.data.put(kind, data);
	}

	private Map<KindInformation, DataTable> data;
	private long[] id;
	private int x;
	private int y;
	private int width;
	private int height;
}
