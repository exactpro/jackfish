////////////////////////////////////////////////////////////////////////////////
//  Copyright (c) 2009-2015, Exactpro Systems, LLC
//  Quality Assurance & Related Development for Innovative Trading Systems.
//  All rights reserved.
//  This is unpublished, licensed software, confidential and proprietary
//  information which is the property of Exactpro Systems, LLC or its licensors.
////////////////////////////////////////////////////////////////////////////////

package com.exactprosystems.jf.documents.matrix.parser.items;

import com.exactprosystems.jf.api.app.Mutable;
import java.util.ArrayList;
import java.util.Collection;

public class MutableArrayList<T extends Mutable> extends ArrayList<T> implements Mutable
{
	private static final long	serialVersionUID	= -61727654712092442L;

	public MutableArrayList()
	{
		super();
		this.changed = false;
	}

	public MutableArrayList(Collection<? extends T> c)
	{
		super(c);
		this.changed = false;
	}

	public MutableArrayList(int initialCapacity)
	{
		super(initialCapacity);
		this.changed = false;
	}

	public void from(Collection<? extends T> c)
	{
		clear();
		addAll(c);
	}
	
	//==============================================================================================
	// implements ArrayList
	//==============================================================================================

	@Override
	public void add(int index, T element)
	{
		this.changed = true;
		super.add(index, element);
	}
	@Override
	public boolean add(T e)
	{
		this.changed = true;
		return super.add(e);
	}
	
	@Override
	public boolean addAll(Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		return super.addAll(c);
	}
	
	@Override
	public boolean addAll(int index, Collection<? extends T> c)
	{
		this.changed = this.changed || c.size() > 0;
		return super.addAll(index, c);
	}
	
	@Override
	public void clear()
	{
		this.changed = this.changed || size() > 0;
		super.clear();
	}
	
	@Override
	public T remove(int index)
	{
		T removed = super.remove(index);
		this.changed = this.changed || removed != null;
		return removed;
	}
	
	@Override
	public boolean removeAll(Collection<?> c)
	{
		boolean res = super.removeAll(c);
		this.changed = this.changed || res;
		return res;
	}
	
	@Override
	public boolean remove(Object o)
	{
		boolean res = super.remove(o);
		this.changed |= res;
		return res;
	}
	
	@Override
	public T set(int index, T element)
	{
		this.changed = true;
		return super.set(index, element);
	}
	
	
	
	//==============================================================================================
	// implements Mutable
	//==============================================================================================
	
	@Override
	public boolean isChanged()
	{
		if (this.changed)
		{
			return true;
		}
		for (T element : this)
		{
			if (element.isChanged())
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public void saved()
	{
		this.changed = false;
		for (T element : this)
		{
			element.saved();
		}
	}
	
	private boolean changed;
}
