////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.api.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class Holder<T>
{
	public Holder(T value)
	{
		this.locators = new HashMap<LocatorKind, Locator>();
		this.list = new ArrayList<T>();
		
		setIndex(0);
		setValue(value);
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