////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.awt.Rectangle;
import java.io.Serializable;
import java.util.*;

public class OperationResult implements Serializable
{
	private static final long			serialVersionUID	= -2015741070415094348L;

	private boolean						ok					= false;
	private String						text				= null;
	private Map<String, String>			map					= null;
	private Map<String, ValueAndColor>	colorMap			= null;
	private String[][]					array				= null;
	private Rectangle 					rectangle 			= null;

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
		this.map = new LinkedHashMap<>();
		this.map.putAll(map);
	}

	public void setColorMap(Map<String, ValueAndColor> colorMap)
	{
		this.colorMap = new LinkedHashMap<>();
		this.colorMap.putAll(colorMap);
	}

	public void setArray(String[][] a)
	{
		this.array = a;
	}

	public void setList(List<String> list)
	{
		this.map = new LinkedHashMap<>();
		for (int i = 0; i < list.size(); i++)
		{
			String s = list.get(i);
			this.map.put(String.valueOf(i), s);
		}
	}

	public void setRectangle(Rectangle rectangle)
	{
		this.rectangle = rectangle;
	}
	

	public boolean isOk()
	{
		return this.ok;
	}

	public Object getValue()
	{
		if (this.text != null)
		{
			return this.text;
		}
		if (this.map != null)
		{
			return this.map;
		}
		if (this.colorMap != null)
		{
			return this.colorMap;
		}
		if (this.array != null)
		{
			return this.array;
		}
		if (this.rectangle != null)
		{
			return this.rectangle;
		}
		
		return this.ok;
	}
	
	
	public String humanablePresentation()
	{
		StringBuilder builder = new StringBuilder("ok [" + this.ok + "]");
		if (this.text != null)
		{
			builder.append(" text [").append(this.text).append("];");
		}
		if (this.rectangle != null)
		{
			builder.append(" rectangle [").append(this.rectangle).append("];");
		}
		if (this.map != null)
		{
			builder.append(" map [").append(this.map).append("];");
		}
		if (this.colorMap != null)
		{
			builder.append(" color map [").append(this.colorMap).append("];");
		}
		if (this.array != null)
		{
			builder.append(" array [").append(Arrays.deepToString(this.array)).append("];");
		}
		return builder.toString();
	}


	@Override
	public String toString()
	{
		return "OpRes[ok=" + this.ok + ", text="  + this.text + "]";
	}
}
