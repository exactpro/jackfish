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

package com.exactprosystems.jf.common.evaluator;

import com.exactprosystems.jf.api.common.i18n.R;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.*;

public class LinkedProperties extends Properties
{
	private Map<Object, Object> linkMap = new LinkedHashMap<>();

	public LinkedProperties()
	{
		super();
	}

	@Override
	public synchronized Object put(Object key, Object value)
	{
		return this.linkMap.put(key, value);
	}

	@Override
	public synchronized boolean contains(Object value)
	{
		return this.linkMap.containsValue(value);
	}

	@Override
	public boolean containsValue(Object value)
	{
		return this.linkMap.containsValue(value);
	}

	@Override
	public synchronized Enumeration<Object> elements()
	{
		throw new UnsupportedOperationException(R.LINKED_PROPS_ELEMENTS_SO_OLD.get());
	}

	@Override
	public Set<Map.Entry<Object, Object>> entrySet()
	{
		return this.linkMap.entrySet();
	}

	@Override
	public synchronized void clear()
	{
		this.linkMap.clear();
	}

	@Override
	public synchronized boolean containsKey(Object key)
	{
		return this.linkMap.containsKey(key);
	}

	@Override
	public void store(Writer writer, String comments) throws IOException
	{
		BufferedWriter bw = (writer instanceof BufferedWriter) ? (BufferedWriter) writer : new BufferedWriter(writer);
		synchronized (this)
		{
			for (Map.Entry<Object, Object> entry : this.linkMap.entrySet())
			{
				bw.write(entry.getKey() + "=" + entry.getValue());
				bw.newLine();
			}
		}
		bw.flush();
	}
}

