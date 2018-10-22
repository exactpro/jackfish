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

import com.exactprosystems.jf.api.error.app.ElementNotFoundException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Holder<T>
{
	public Holder(T value, Locator locator)
	{
		this.locators = new HashMap<LocatorKind, Locator>();
		this.list = new ArrayList<T>();
		
		put(LocatorKind.Element, 	locator);
		setIndex(0);
		setValue(value);
	}
	
	@Override
	public String toString()
	{
		return getClass().getSimpleName() + "["
				+ " locators=" + this.locators
				+ " list=" + this.list
				+ "]";
	}
	
	public void setValue(T value)
	{
		this.list.clear();
		if (value != null)
		{
			this.list.add(value);
		}
	}

	public void setValues(List<T> values)
	{
		this.list.clear();
		if (values != null)
		{
			this.list.addAll(values);
		}
	}

	public T getValue() throws Exception
	{
		if (isEmpty())
		{
			throw new ElementNotFoundException(locators.get(LocatorKind.Element));
		}

		if (index >= this.list.size() || index < 0)
		{
			throw new Exception("Wrong index in 'use(" + index + ")' cause size is " +  this.list.size() + " for locator " + locators.get(LocatorKind.Element));
		}

		return this.list.get(this.index);
	}

	public boolean isEmpty()
	{
		return this.list.isEmpty();
	}
	
	public int size()
	{
		return this.list.size();
	}
	
	public void setIndex(int index)
	{
		this.index = index;
	}
	
	public void reset()
	{
		this.index = 0;
		this.list.clear();
	}
	
	public void put(LocatorKind kind, Locator locator)
	{
		reset();
		this.locators.put(kind, locator);
	}
	
	public Locator get(LocatorKind kind)
	{
		return this.locators.get(kind);
	}

	private Map<LocatorKind, Locator> locators;
	private List<T> list;
	private int index = 0;
}