////////////////////////////////////////////////////////////////////////////////
// Copyright (c) 2009-2017, Exactpro Systems
// Quality Assurance & Related Software Development for Innovative Trading Systems.
// London Stock Exchange Group.
// All rights reserved.
// This is unpublished, licensed software, confidential and proprietary
// information which is the property of Exactpro Systems or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.common.evaluator;

import com.exactprosystems.jf.api.common.i18n.R;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class LinkedProperties extends Properties
{
	private Map<Object, Object>	linkMap				= new LinkedHashMap<>();

	public LinkedProperties()
	{
	}
	
	@Override
	public synchronized Object put(Object key, Object value)
	{
		return linkMap.put(key, value);
	}

	@Override
	public synchronized boolean contains(Object value)
	{
		return linkMap.containsValue(value);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return linkMap.containsValue(value);
	}

	@Override
	public synchronized Enumeration<Object> elements()
	{
		throw new UnsupportedOperationException(R.LINKED_PROPS_ELEMENTS_SO_OLD.get());
	}

	@Override
	public Set<Map.Entry<Object, Object>> entrySet()
	{
		return linkMap.entrySet();
	}

	@Override
	public synchronized void clear()
	{
		linkMap.clear();
	}

	@Override
	public synchronized boolean containsKey(Object key)
	{
		return linkMap.containsKey(key);
	}

	@Override
	public void store(Writer writer, String comments) throws IOException
	{
		BufferedWriter bw = (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
		synchronized (this)
		{
			for (Map.Entry<Object, Object> entry : linkMap.entrySet())
			{
				bw.write(entry.getKey() + "=" + entry.getValue());
				bw.newLine();
			}
		}
		bw.flush();
	}
	private static final long	serialVersionUID	= 1L;
}

